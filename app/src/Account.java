/*
 * Date: October 2. 2026
 * Version: 1.0
 * Purpose: This program is the account page for the application.
 */

package myjavapackage;

public class Account{

    //variables
    private String email = null;
    private String username = null;
    private String password = null;
    private String firstName = null;
    private String lastName = null;
    private float userHeight = 0.0;
    private float userWeight = 0.0;
    private int accountID = 0;
    private String userExperienceLevel = null;

    // constructor methods
    Account(String email, String username, String password, String firstName, String lastName, float userHeight, float userWeight, int accountID, String userExperienceLevel){
        this.email = email;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountID = accountID;
        this.userHeight = userHeight;
        this.userWeight = userWeight;
        this.userExperienceLevel = userExperienceLevel;
    }
    // getter methods
    public String getEmail(){
        return email;
    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public float getUserHeight(){
        return userHeight;
    }
    public float getUserWeight(){
        return userWeight;
    }
    public String getUserExperienceLevel(){
        return userExperienceLevel;
    }

    // setter methods
    public void setEmail(String email){
        this.email = email;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public void setAccountID(int accountID){
        this.accountID = accountID;
    }
    public void setUserHeight(float userHeight){
        this.userHeight = userHeight;
    }
    public void setUserWeight(float userWeight){
        this.userWeight = userWeight;
    }
    public void setUserExperienceLevel(String userExperienceLevel){
        this.userExperienceLevel = userExperienceLevel;
    }
} //end of account class
