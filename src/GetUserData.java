//class by Louis H
// modified by Henry L

//---[ Class Imports ]---

import java.io.*;

public class GetUserData {
    //---[ Class Variables ]---

    // link to bankValue object
    //stock array here



    /*
    Generates a new file if one cannot be found
    */
    private void generateNew(String fileName) { //method to create a new populated file in case file cannot be found.
        try {
            File newFile = new File(fileName);
            newFile.createNewFile();
            System.out.println("File created.");
        } catch (IOException e) {
            System.out.println("File could not be created.");
        }
        // New user?
    }


    /*
    Write changes made to HashMap to file.
    */
    public void save(user activeUser) {
        String fileName = activeUser.name + ".user";
        try {
            File fileOne = new File(fileName);
            FileOutputStream fos = new FileOutputStream(fileOne);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(activeUser);
            oos.flush();
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            System.out.println("Could not find the file!");
            generateNew(fileName);
            // Call self?
            save(activeUser);
        } catch (IOException e) {
            System.out.println("IOException Error.");
            e.printStackTrace();
        }
    }

    /*
    Load User from file
     */
    public  user load(String fileName) {
        // Define new blank user
        // New window to get user stuff?
        String[] stocks = {};
        int[] amounts = {};
        String[] bankdetails = {"01234 56789", "visa"};
        user activeUser = new user("", 0, stocks, amounts, bankdetails, "");
        try {
            File toRead = new File(fileName);
            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);


            activeUser = (user) ois.readObject();

            ois.close();
            fis.close();

        } catch (FileNotFoundException e) {
            System.out.println("Error. Could not find file. New file being generated");
            generateNew(fileName);
            save(activeUser);
        } catch (Exception e) {
            System.out.println("Other error");
            e.printStackTrace();
        }
        return activeUser;
    }
}

