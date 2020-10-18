import javax.xml.transform.Result;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Order implements Serializable{
    private String name;
    private int rate;
    private  int quantity;

    public Order(String name, int rate, int quantity) {
        this.name = name;
        this.rate = rate;
        this.quantity = quantity;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName(){
        return name;
    }
    public int getRate() {
        return rate;
    }

    public int getQuantity() {
        return quantity;
    }
}

class Item implements Serializable{
    private int rate;
    private  int quantity;

    public Item(int rate, int quantity) {
        this.rate = rate;
        this.quantity = quantity;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRate() {
        return rate;
    }

    public int getQuantity() {
        return quantity;
    }
}

public class Server {
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(8081);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        while (true) {
            Socket s = null;
            try {
                s = ss.accept();
                System.out.println("New Client Connected:" + s);
                DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
                DataInputStream inFromClient = new DataInputStream(s.getInputStream());
                ObjectOutputStream os=new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream is=new ObjectInputStream(s.getInputStream());
                System.out.println("Assigning Thread to Client");
                Thread t = new ClientHandler(s, inFromClient, outToClient,os,is);
                executor.execute(t);
                //t.start();
            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }
        }
    }
}
class ClientHandler extends Thread {
    final DataInputStream inFromClient;
    final DataOutputStream outToClient;
    final Socket s;
    final ObjectInputStream is;
    final ObjectOutputStream os;

    public ClientHandler(Socket s, DataInputStream inFromClient, DataOutputStream outToClient,ObjectOutputStream os,ObjectInputStream is) throws Exception {
        this.s = s;
        this.inFromClient = inFromClient;
        this.outToClient = outToClient;
        this.os = os;
        this.is = is;
    }

    public void run() {
        try {
            Scanner sc = new Scanner(System.in);
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sdl_lab?autoReconnect=true&useSSL=false", "root", "Tejas@123");
            Statement stmt = conn.createStatement();
            String choice,user_type="N";
            while(true){
                Boolean Authentication=false;
                String usern = "";
                while(!Authentication){
                    outToClient.writeUTF("1.Login\n2.Sign Up:");
                    choice = inFromClient.readUTF();
                    switch(choice)
                    {
                        case "1":{
                            Customer c = new Customer();
                            outToClient.writeUTF("Enter Username:");
                            String username = inFromClient.readUTF();
                            outToClient.writeUTF("Enter Password:");
                            String password = inFromClient.readUTF();
                            PreparedStatement ps = conn.prepareStatement("select username,name,contactNumber,flatNo,user_type from Users where username = ? and password = ?");
                            ps.setString(1, username);
                            ps.setString(2, password);
                            try{ResultSet rs = ps.executeQuery();

                                if (rs.next()) {
                                outToClient.writeUTF("UserName:" + rs.getString(1) + "\nName:" + rs.getString(2) + "\nContact: " + rs.getString(3));
                                user_type=rs.getString(5);
                                Authentication = true;
                                usern = rs.getString(1);
                                //System.out.println(user_type+usern);
                            } else {
                                outToClient.writeUTF("Invalid Credentials!!");
                            }}
                            catch (Exception e)
                            {outToClient.writeUTF("Error!");
                                System.out.println(e);
                            }
                            //outToClient.writeBytes(usern);

                            //os.writeObject(c);
                            outToClient.writeUTF(usern);
                            outToClient.writeBoolean(Authentication);
                            outToClient.writeUTF(user_type);
                            break;
                        }
                        case "2": {
                            outToClient.writeUTF("Enter Name:");
                            String name = inFromClient.readUTF();
                            outToClient.writeUTF("Enter username:");
                            String username = inFromClient.readUTF();
                            outToClient.writeUTF("Enter password:");
                            String password = inFromClient.readUTF();
                            outToClient.writeUTF("Enter Contact_no:");
                            String contact = inFromClient.readUTF();
                            outToClient.writeUTF("Enter Flat No:");
                            int flatNo = inFromClient.readInt();
                            outToClient.writeUTF("User Type(Admin A/Normal N):");
                            String type = inFromClient.readUTF();
                            PreparedStatement ps = conn.prepareStatement("insert into Users(username,name,password,flatno,contactNumber,user_type) values(?,?,?,?,?,?)");
                            ps.setString(1,username);
                            ps.setString(2,name);
                            ps.setString(3,password);
                            ps.setInt(4,flatNo);
                            ps.setString(5,contact);
                            ps.setString(6,type);
                            try {
                                int rs = ps.executeUpdate();
                                outToClient.writeUTF("Account Created!!\nLogin to continue!!");
                            }catch(Exception e)
                            {
                                outToClient.writeUTF(String.valueOf(e));
                            }
                            break;
                        }
                        default:
                            System.out.println("Incorrect Choice!!");
                            break;
                    }
                }
                //user_type=inFromClient.readUTF();
                System.out.println(user_type);
                if(user_type.equals("N"))
                {
                    //outToClient.writeUTF("------MENU------");
                    //outToClient.writeUTF("1.View Items\n2.Add Items to cart\n3.View Cart\n4.Confirm Order\n5.View History\n");
                    while(true){
                        choice=inFromClient.readUTF();
                        System.out.println(choice);
                        if(choice.equals("0")){
                            break;
                        }
                        switch(choice)
                        {
                            case "Send items": {
                                HashMap<String,Item> stk = new HashMap<String,Item>();
                                Statement st = conn.createStatement();
                                ResultSet rs = st.executeQuery("select * from Stock");
                                while(rs.next()){
                                    if(rs.getInt(3) >= 0){
                                        stk.put(rs.getString(2),new Item(rs.getInt(4),rs.getInt(3)));
                                    }
                                }
                                System.out.println(stk);
                                os.reset();
                                os.writeObject(stk);
                                break;
                            }
                            case "Add to cart":{
                                String us = inFromClient.readUTF();
                                String item = inFromClient.readUTF();
                                Integer quan = inFromClient.readInt();
                                Integer rat = inFromClient.readInt();
                                PreparedStatement p = conn.prepareStatement("select * from Cart where item_name=? and username=?");
                                p.setString(1,item);
                                p.setString(2,us);
                                ResultSet r = p.executeQuery();
                                if(r.next()){
                                    PreparedStatement p1 = conn.prepareStatement("update Cart set qty=? where item_name=? and username=?");
                                    p1.setString(2,item);
                                    p1.setInt(1,quan);

                                    p1.setString(3,us);
                                    p1.executeUpdate();

                                } else {
                                    PreparedStatement p2 = conn.prepareStatement("insert into Cart values(?,?,?,?)");
                                    p2.setString(1,item);
                                    p2.setInt(2,quan);
                                    p2.setInt(3,rat);
                                    p2.setString(4,us);
                                    p2.executeUpdate();
                                }
                                outToClient.writeUTF("Item added in cart");
                                break;
                            }
                            case "View cart":{
                                String us = inFromClient.readUTF();
                                PreparedStatement p = conn.prepareStatement("select item_name,qty,rate from Cart where username=?");
                                p.setString(1,us);
                                ResultSet r = p.executeQuery();

                                while(r.next()){
                                    outToClient.writeUTF("con");
                                    outToClient.writeUTF(r.getString(1));
                                    outToClient.writeInt(r.getInt(2));
                                    outToClient.writeInt(r.getInt(3));
                                }
                                outToClient.writeUTF("dis");
                                break;
                            }
                            case "Confirming order": synchronized (this){
                                //HashMap<String,Integer> cart = (HashMap<String, Integer>) is.readObject();
                                String us = inFromClient.readUTF();
                                PreparedStatement p = conn.prepareStatement("select item_name,qty,rate from Cart where username=?");
                                p.setString(1,us);
                                ResultSet r5 = p.executeQuery();
                                System.out.println("Item fetched from cart");
                                LocalDate dd = LocalDate.now();
                                PreparedStatement st = conn.prepareStatement("insert into Orders values(null,?,?,0);");
                                PreparedStatement st1 = conn.prepareStatement("select order_id from Orders where username=? order by order_id desc limit 1");
                                PreparedStatement st5 = conn.prepareStatement("update Orders set total=? where order_id=?");
                                st.setString(1,us);
                                st.setString(2,dd.toString());
                                st.executeUpdate();
                                st1.setString(1,us);
                                ResultSet r = st1.executeQuery();
                                String order_id = "";
                                if(r.next()){
                                    order_id = r.getString(1);
                                }
                                //Statement sta = conn.createStatement();
                                int total=0;
                                PreparedStatement p2 = conn.prepareStatement("update Stock set qty = qty - ? where item_name=?");
                                PreparedStatement st2 = conn.prepareStatement("insert into ItemList values(?,?,?,?)");
                                while(r5.next()){
                                    st2.setInt(1,Integer.parseInt(order_id));
                                    st2.setString(2,r5.getString(1));
                                    st2.setInt(3,r5.getInt(2));
                                    st2.setInt(4,r5.getInt(3));
                                    p2.setInt(1,r5.getInt(2));
                                    p2.setString(2,r5.getString(1));
                                    st2.executeUpdate();
                                    p2.executeUpdate();
                                    total+=r5.getInt(3)*r5.getInt(2);
                                    st5.setInt(1,total);
                                    st5.setInt(2,Integer.parseInt(order_id));
                                    st5.executeUpdate();
                                }
                                PreparedStatement p4 = conn.prepareStatement("delete from Cart where username=?");
                                p4.setString(1,us);
                                p4.executeUpdate();
                                outToClient.writeUTF("Confirmed order");
                                break;
                            }
                            case "Recent orders": {
                                String us = inFromClient.readUTF();
                                PreparedStatement p = conn.prepareStatement("select name,flatno,contactNumber,order_id,dateOfOrder,total from Users natural join Orders where username=?;");
                                p.setString(1,us);
                                PreparedStatement p4 = conn.prepareStatement("select name,flatno,contactNumber from Users username=?;");
                                p4.setString(1,us);


                                ResultSet r2 = p.executeQuery();
                                //System.out.println(r);
                                if(r2.next()){
                                    outToClient.writeUTF(r2.getString(1));
                                    outToClient.writeInt(r2.getInt(2));
                                    outToClient.writeUTF(r2.getString(3));
                                }
                                ResultSet r =p.executeQuery();
                                while (r.next()){
                                    outToClient.writeUTF("con");
                                    outToClient.writeInt(r.getInt(4));
                                    PreparedStatement p2 = conn.prepareStatement("select item_name,qty,rate from ItemList where order_id=?");
                                    p2.setInt(1,r.getInt(4));
                                    ResultSet r1 = p2.executeQuery();
                                    HashMap<String,Item> cl = new HashMap<String, Item>();
                                    int total=0;
                                    while(r1.next()){
                                        cl.put(r1.getString(1),new Item(r1.getInt(3),r1.getInt(2)));
                                        total+=(r1.getInt(3)*r1.getInt(2));
                                    }
                                    os.reset();
                                    os.writeObject(cl);
                                    outToClient.writeInt(total);
                                }
                                outToClient.writeUTF("dis");
                                break;
                            }

                        }
                    };

                }
                else
                {
                    while(true){
                        choice=inFromClient.readUTF();
                        System.out.println(choice);
                        if(choice.equals("0")){
                            break;
                        }
                        switch(choice)
                        {
                            case "Send stock": {
                                HashMap<String,Item> stk = new HashMap<String,Item>();
                                Statement st = conn.createStatement();
                                ResultSet rs = st.executeQuery("select * from Stock");
                                while(rs.next()){
                                    if(rs.getInt(3) >= 0){
                                        stk.put(rs.getString(2),new Item(rs.getInt(4),rs.getInt(3)));
                                    }
                                }
                                System.out.println(stk);
                                os.reset();
                                os.writeObject(stk);
                                break;
                            }
                            case "Add stock": {
                                String itemName = inFromClient.readUTF();
                                PreparedStatement p = conn.prepareStatement("select * from Stock where item_name=?");
                                p.setString(1,itemName);
                                ResultSet r = p.executeQuery();
                                if(r.next()){
                                    outToClient.writeUTF("Item already in list");
                                    break;
                                }else {
                                    outToClient.writeUTF("Not in list");
                                }
                                int qu = inFromClient.readInt();
                                int ra = inFromClient.readInt();
                                PreparedStatement p2 = conn.prepareStatement("insert into Stock values(null,?,?,?)");
                                p2.setString(1,itemName);
                                p2.setInt(2,qu);
                                p2.setInt(3,ra);

                                System.out.println("Item added");
                                outToClient.writeUTF("Item added");
                                break;
                            }
                            case "Update stock": {
                                String itemName = inFromClient.readUTF();
                                PreparedStatement p = conn.prepareStatement("select * from Stock where item_name=?");
                                p.setString(1,itemName);
                                ResultSet r = p.executeQuery();
                                if(!r.next()){
                                    outToClient.writeUTF("Not in list");
                                    break;
                                }else {
                                    outToClient.writeUTF("Item in list");
                                }
                                int qu = inFromClient.readInt();
                                int ra = inFromClient.readInt();
                                PreparedStatement p2 = conn.prepareStatement("update Stock set qty=? where item_name=?");
                                PreparedStatement p3 = conn.prepareStatement("update Stock set rate=? where item_name=?");
                                p2.setString(2,itemName);
                                p3.setString(2,itemName);
                                p2.setInt(1,qu);
                                p3.setInt(1,ra);
                                //p2.setInt(3,ra);
                                p2.executeUpdate();
                                p3.executeUpdate();
                                System.out.println("Item updated");
                                outToClient.writeUTF("Item updated");
                                break;
                            }
                            case "Remove stock": {
                                String itemName = inFromClient.readUTF();
                                PreparedStatement p = conn.prepareStatement("select * from Stock where item_name=?");
                                p.setString(1,itemName);
                                ResultSet r = p.executeQuery();
                                if(!r.next()){
                                    outToClient.writeUTF("Item not in stock");
                                    break;
                                }else {
                                    PreparedStatement p2 = conn.prepareStatement("delete from Stock where item_name=?");
                                    p2.setString(1,itemName);
                                    p2.executeUpdate();
                                    outToClient.writeUTF("Item removed from stock");
                                }
                                break;
                            }
                            case "View orders": {
                                //String us = inFromClient.readUTF();
                                PreparedStatement p = conn.prepareStatement(" select username,name,flatno,contactNumber,order_id,dateOfOrder from Users natural join Orders");
                                ResultSet r =p.executeQuery();
                                while (r.next()){
                                    outToClient.writeUTF("con");
                                    outToClient.writeUTF(r.getString(2));
                                    outToClient.writeInt(r.getInt(3));
                                    outToClient.writeUTF(r.getString(4));
                                    outToClient.writeUTF(r.getString(6));
                                    PreparedStatement p2 = conn.prepareStatement("select item_name,qty,rate from ItemList where order_id=?");
                                    p2.setInt(1,r.getInt(5));
                                    ResultSet r1 = p2.executeQuery();
                                    HashMap<String,Item> cl = new HashMap<String, Item>();
                                    int total=0;
                                    while(r1.next()){
                                        cl.put(r1.getString(1),new Item(r1.getInt(3),r1.getInt(2)));
                                        total+=(r1.getInt(3)*r1.getInt(2));
                                    }
                                    os.reset();
                                    os.writeObject(cl);
                                    outToClient.writeInt(total);
                                }
                                outToClient.writeUTF("dis");
                                break;
                            }
                        }
                    };
                }

               //outToClient.writeUTF("1.Exit\n2.Continue");
               choice = inFromClient.readUTF();
                if(choice.equals("0")){
                    break;
                }
            };
        }
        catch (Exception e) {
            System.out.println(e);
        }
        try{
            this.inFromClient.close();
            this.outToClient.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}