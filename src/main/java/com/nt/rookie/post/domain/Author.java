package com.nt.rookie.post.domain;

import java.util.Date;

public class Author {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private Date birthdate;
    private Date added;

    public Author() {
    }

    public Author(String username, String password, String firstName, String lastName, String email, Date birthdate, Date added) {
        super();
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthdate = birthdate;
        this.added = added;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    @Override
    public String toString() {
        return "Author [username=" + username + ", password=" + password + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", birthdate=" + birthdate + ", added=" + added
                + "]";
    }

}
