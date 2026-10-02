/*
 * Date: October 2. 2026
 * Version: 1.0
 * Purpose: This program is the account page for the application.
 */

package myjavapackage;

public class Account{

    //variables
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private float userHeight;
    private float userWeight;
    private int accountID;
    private String userExperienceLevel;

    // getter methods
    public String getEmail(){
        return email;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public float getUserHeight() {
        return userHeight;
    }
    public float getUserWeight() {
        return userWeight;
    }
    public String getUserExperienceLevel() {
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
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }
    public void setUserHeight(float userHeight) {
        this.userHeight = userHeight;
    }
    public void setUserWeight(float userWeight) {
        this.userWeight = userWeight;
    }
    public void setUserExperienceLevel(String userExperienceLevel) {
        this.userExperienceLevel = userExperienceLevel;
    }
} //end of account class