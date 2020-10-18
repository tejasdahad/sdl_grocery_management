import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;

class Customer implements Serializable {
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void printItems(HashMap<String,Item> stock) {
        for (String key : stock.keySet()) {
            System.out.println("Item Name: " + key);
            System.out.println("Rate: " + stock.get(key).getRate());
        if (stock.get(key).getQuantity() > 0) {
            System.out.println("Available: Yes");
        } else {
            System.out.println("Available: No");
        }
            System.out.println("\n");
        }
    }

}

public class Client {
    public static void main(String args[]) throws IOException
    {
        String serveraddress = "127.0.0.1";
        int port =8081;
        String user_type = "N";
        Scanner sc=new Scanner(System.in);
        String choice;
        Socket client = new Socket(serveraddress,port);
        System.out.println("Connected to: "+client.getRemoteSocketAddress());
        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
        DataInputStream inFromServer = new DataInputStream(client.getInputStream());
        ObjectOutputStream os=new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream is=new ObjectInputStream(client.getInputStream());
        while(true){
            Boolean Authentication=false;
            Customer c = new Customer();
            do {
                System.out.println(inFromServer.readUTF());
                choice = sc.nextLine();
                outToServer.writeUTF(choice);
                switch (choice) {
                    case "1": {
                        System.out.println(inFromServer.readUTF());
                        String username = sc.nextLine();
                        outToServer.writeUTF(username);
                        System.out.println(inFromServer.readUTF());
                        String password = sc.nextLine();
                        outToServer.writeUTF(password);
                        System.out.println(inFromServer.readUTF());
                        String userna = inFromServer.readUTF();

                        Authentication = inFromServer.readBoolean();
                        //System.out.println(Authentication);
                        user_type = inFromServer.readUTF();
                        c.setUsername(userna);
                        //System.out.println(userna);
                        break;
                    }
                    case "2": {
                        System.out.println(inFromServer.readUTF());
                        String name = sc.nextLine();
                        outToServer.writeUTF(name);
                        System.out.println(inFromServer.readUTF());
                        String username = sc.nextLine();
                        outToServer.writeUTF(username);
                        System.out.println(inFromServer.readUTF());
                        String password = sc.nextLine();
                        outToServer.writeUTF(password);
                        System.out.println(inFromServer.readUTF());
                        String contact = sc.nextLine();
                        outToServer.writeUTF(contact);
                        System.out.println(inFromServer.readUTF());
                        int flatNo = Integer.parseInt(sc.nextLine());
                        outToServer.writeInt(flatNo);
                        System.out.println(inFromServer.readUTF());
                        String type = sc.nextLine();
                        outToServer.writeUTF(type);
                        System.out.println(inFromServer.readUTF());
                        break;
                    }
                    default:
                        System.out.println("Incorrect Choice!!");
                        break;
                }
            }while(!Authentication);
            //outToServer.writeUTF(user_type);
            //System.out.println(user_type);
            if(user_type.equals("N"))
            {
                HashMap<String,Item> stk = new HashMap<String, Item>();
                while(true) {
                    System.out.println("Menu");
                    System.out.println("1.View Items\n2.Add Items to cart\n3.View Cart\n4.Confirm Order\n5.View History\n");
                    choice = sc.nextLine();
                    if(choice.equals("0")){
                        break;
                    }
                    //outToServer.writeUTF(choice);
                    switch(choice)
                    {
                        case "1": {
                            outToServer.writeUTF("Send items");
                            try {
                                stk = (HashMap<String,Item>) is.readObject();
                                c.printItems(stk);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case "2": {
                            outToServer.writeUTF("Send items");
                            try {
                                stk = (HashMap<String,Item>) is.readObject();
                                //c.printItems(stk);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }

                            String str;
                            Integer quan;
                            System.out.println("What would you like to buy?");
                            str= sc.next();
                            if(stk.containsKey(str) && stk.get(str).getQuantity()!=0) {
                                System.out.print("Enter quantity:");
                                quan = sc.nextInt();
                                if(stk.get(str).getQuantity()-quan>=0){
                                    outToServer.writeUTF("Add to cart");
                                    outToServer.writeUTF(c.getUsername());
                                    outToServer.writeUTF(str);
                                    outToServer.writeInt(quan);
                                    outToServer.writeInt(stk.get(str).getRate());
                                    str = inFromServer.readUTF();
                                    System.out.println(str);
                                }else{
                                    System.out.println("Stock is less than your requirement!");
                                    break;
                                }
                            } else {
                                System.out.println("Item not available or is out of stock!");
                                break;
                            }
                            //c.place_order(stk);
                            //System.out.println(c.getCls());
                            break;
                        }
                        case "3": {
                            outToServer.writeUTF("View cart");
                            outToServer.writeUTF(c.getUsername());

                            int total=0;
                            System.out.println("Item\t\tQty\t\tRate");
                            while (true){
                                String s = inFromServer.readUTF();
                                if(s.equals("dis")){
                                    break;
                                }
                                String item = inFromServer.readUTF();
                                int qu = inFromServer.readInt();
                                int ra = inFromServer.readInt();
                                System.out.println(item+"\t\t"+qu+"\t\t"+ra);
                                total+=ra*qu;
                            }
                            System.out.println("Total:" + total);
                            break;
                        }
                        case "4":{
                            outToServer.writeUTF("Confirming order");
                            //os.reset();
                            //os.writeObject(c.getCls());
                            outToServer.writeUTF(c.getUsername());
                            String st = inFromServer.readUTF();
                            System.out.println(st);
                            break;
                        }
                        case "5":{
                            outToServer.writeUTF("Recent orders");
                            outToServer.writeUTF(c.getUsername());
                            System.out.println("Name:" + inFromServer.readUTF());
                            System.out.println("Flat No:" + inFromServer.readInt());
                            System.out.println("Contact Number: " + inFromServer.readUTF());
                            while(true) {
                                String ch = inFromServer.readUTF();
                                if(ch.equals("dis")){
                                    break;
                                }
                                int ordId = inFromServer.readInt();
                                System.out.println("Order ID :" + ordId);
                                try {
                                    HashMap<String,Item> car = (HashMap<String, Item>) is.readObject();
                                    System.out.println("Item\t\tQty\t\tRate");
                                    for(String i : car.keySet()){
                                        System.out.println(i+"\t\t"+car.get(i).getQuantity()+ "\t\t"+car.get(i).getRate());
                                    }
                                    int tot = inFromServer.readInt();
                                    System.out.println("Total:" + tot + "\n");
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }

                            }
                            break;
                        }
                    }
                };
            }
            else
            {
                HashMap<String,Item> stock = new HashMap<String, Item>();
                while(true) {
                    System.out.println("Menu");
                    System.out.println("1.View Stock\n2.Add Stock\n3.Update Stock\n4.Remove Stock\n5.View Orders\n");
                    choice = sc.nextLine();
                    if(choice.equals("0")){
                        break;
                    }
                    //outToServer.writeUTF(choice);
                    switch(choice)
                    {
                        case "1": {
                            outToServer.writeUTF("Send stock");
                            try {
                                stock = (HashMap<String,Item>) is.readObject();
                                System.out.println("Item\t\tQty\t\tRate");
                                for(String i: stock.keySet()){
                                    System.out.println(i+"\t\t"+stock.get(i).getQuantity()+"\t\t"+stock.get(i).getRate());
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case "2":{
                            outToServer.writeUTF("Add stock");
                            String item;
                            System.out.println("Enter name of item:");
                            item = sc.nextLine();
                            outToServer.writeUTF(item);
                            String res = inFromServer.readUTF();
                            if(res.equals("Item already in list")){
                                System.out.println(res);
                                break;
                            }
                            int qu,ra;
                            System.out.println("Enter quantity");
                            qu = sc.nextInt();
                            outToServer.writeInt(qu);
                            System.out.println("Enter rate");
                            ra = sc.nextInt();
                            outToServer.writeInt(ra);
                            item = inFromServer.readUTF();
                            System.out.println(item);
                            break;
                        }
                        case "3":{
                            outToServer.writeUTF("Update stock");
                            String item;
                            System.out.println("Enter name of item:");
                            item = sc.nextLine();
                            outToServer.writeUTF(item);
                            String res = inFromServer.readUTF();
                            if(res.equals("Not in list")){
                                System.out.println(res);
                                break;
                            }
                            int qu,ra;
                            System.out.println("Enter new quantity");
                            qu = sc.nextInt();
                            outToServer.writeInt(qu);
                            System.out.println("Enter new rate");
                            ra = sc.nextInt();
                            outToServer.writeInt(ra);
                            item = inFromServer.readUTF();
                            System.out.println(item);
                            break;
                        }
                        case "4":{
                            outToServer.writeUTF("Remove stock");
                            String item;
                            System.out.println("Enter name of item:");
                            item = sc.nextLine();
                            outToServer.writeUTF(item);
                            String res = inFromServer.readUTF();
                            System.out.println(res);
                            break;
                        }
                        case "5":{
                            outToServer.writeUTF("View orders");
                            while(true){
                                String ch = inFromServer.readUTF();
                                if(ch.equals("dis")){
                                    break;
                                }
                                System.out.println("Name:"+ inFromServer.readUTF());
                                System.out.println("Flat No:" + inFromServer.readInt());
                                System.out.println("Contact No:" + inFromServer.readUTF());
                                System.out.println("Date of order:" + inFromServer.readUTF());
                                try {
                                    HashMap<String,Item> cl = (HashMap<String, Item>) is.readObject();
                                    System.out.println("Item\t\tQty");
                                    for(String i: cl.keySet()){
                                        System.out.println(i+"\t\t"+cl.get(i).getQuantity());
                                    }
                                    int total = inFromServer.readInt();
                                    System.out.println("Total: " + total);
                                    System.out.println("\n");
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                    }
                };
            }
            System.out.println("Do you want to continue(0 to exit)?");
            choice = sc.nextLine();
            outToServer.writeUTF(choice);
            if(choice.equals("0")){
                break;
            }
        };
    }
}
