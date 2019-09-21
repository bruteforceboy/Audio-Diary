package models;

import static db.DatabaseResources.getConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User
{
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    
    public User()
    {
        
    }
    public User(String username, String password)
    {
        this.username=username;
        this.password=password;
    }
    public User(int id, String firstName, String lastName, String username)
    {
        this.id=id;
        this.firstName=firstName;
        this.lastName=lastName;
        this.username=username;
    }
    
    public User(int id, String firstName, String lastName, String username, String password)
    {
        if(validateFirstName(firstName))
            this.firstName=firstName;
        if(validateLastName(lastName))
            this.lastName=lastName;
        if(validateUsername(username))
            this.username=username;
        if(validatePassword(password))
            this.password=password;
    }
    
    public int getId()
    {
        return id;
    }
    
    public void setFirstName(String firstName)
    {
        if(validateFirstName(firstName))
            this.firstName = firstName;
    }
    
    public String getFirstName()
    {
        return firstName;
    }
    
    public void setLastName(String lastName)
    {
        if(validateLastName(lastName))
            this.lastName = lastName;
    }
    
    public String getLastName()
    {
        return lastName;
    }
    
    public void setUserName(String username)
    {
        if(validateUsername(username))
            this.username = username;
    }
    
    public String getUserName()
    {
        return username;
    }
    
    public void setPassword(String password)
    {
        if(validatePassword(password))
            this.password = password;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public static Boolean validateFirstName(String firstName)
    {
        if (firstName.length() == 0) 
            throw new IllegalArgumentException("First Name can't be empty");
        if (firstName.matches(".*\\d.*"))
            throw new IllegalArgumentException("First Name is invalid");
        if (firstName.length() > 50) 
            throw new IllegalArgumentException("First Name is too long");
        
        return true;
    }
    
    public static Boolean validateLastName(String lastName)
    {
        if(lastName.length() == 0)
            throw new IllegalArgumentException("Last Name can't be empty");
        if(lastName.matches(".*\\d.*"))
            throw new IllegalArgumentException("Last Name is invalid");
        if(lastName.length()>50)
            throw new IllegalArgumentException("Last Name is too long");
        
        return true;
    }
    
    public static Boolean validateUsername(String username)
    {
        if(username.length() == 0)
            throw new IllegalArgumentException("Username can't be empty");
        if(username.length()>30)
            throw new IllegalArgumentException("Username is too long");
        if(usernameExists(username))
            throw new IllegalArgumentException("Username already exists");
        
        return true;
    }
    
    public static Boolean validatePassword(String password)
    {
        if(password.length() == 0)
            throw new IllegalArgumentException("Password can't be empty");
        if(password.length()>20)
            throw new IllegalArgumentException("Password is too long");
        
        return true;
    }
    
    public static Boolean usernameExists(String username)
    {
        Boolean found = false;
        String QUERY = "SELECT ID FROM dbo.[USER] WHERE [Username]=?";
        try(Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY))
        {
            statement.setString(1,username);
            ResultSet rs = statement.executeQuery();
            if(rs.next())
            {
                found=true;
            }
        }   
        catch(SQLException ex)
        {
            System.err.println("An error occured "+ex.getMessage());
        }
        return found;
    }
    
    public static User isValidUser(String username, String password)
    {
        String QUERY = "SELECT * FROM dbo.[USER] WHERE [Username]=? AND [PasswordHash]=HASHBYTES('SHA2_512',?)";
        try(Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY))
        {
            statement.setString(1,username);
            statement.setString(2,password);
            ResultSet rs = statement.executeQuery();
            if(rs.next())
            {
                return new User(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4));
            }
        }
        catch(SQLException ex)
        {
            System.err.println("An error occured "+ex.getMessage());
        }
        return null;
    }
    
    public void insertUser()
    {
        String QUERY = "INSERT INTO dbo.[USER] ([First Name],[Last Name],[Username],[PasswordHash]) VALUES"
                + " (?,?,?,HASHBYTES('SHA2_512',?))";
        String[] returnID = {"ID"};
        try(Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(QUERY,returnID))
        {
            statement.setString(1,firstName);
            statement.setString(2,lastName);
            statement.setString(3,username);
            statement.setString(4,password);
            int affectedRows = statement.executeUpdate();
            if(affectedRows == 0)
                System.err.println("Nothing was inserted");
            else
            {
                ResultSet rs = statement.getGeneratedKeys();
                if(rs.next())
                    id = rs.getInt(1);
            }
        }
        catch(SQLException ex)
        {
            System.err.println("An error occured "+ex.getMessage());
        }
    }
}