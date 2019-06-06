package com.avinashdavid.trivialtrivia.web.data;

public class Registration {

    private String username;
    private String password;
    private String confimpassword;
    private String student_id;
    private String first_name;
    private String middle_initial;
    private String last_name;
    private String dob;
    private String gender;
    private String grade;
    private String course;

    public Registration(String loginID, String passwd, String confirm_passwd, String studentID, String firstName, String middleInitial,
                        String lastName, String dateOfBirth, String gender, String gradeLevel, String course) {
        this.username = loginID;
        this.password = passwd;
        this.confimpassword = confirm_passwd;
        this.student_id = studentID;
        this.first_name = firstName;
        this.middle_initial = middleInitial;
        this.last_name = lastName;
        this.dob = dateOfBirth;
        this.gender = gender;
        this.grade = gradeLevel;
        this.course = course;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConfimpassword() {
        return confimpassword;
    }

    public String getStudent_id() {
        return student_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getMiddle_initial() {
        return middle_initial;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getGrade() {
        return grade;
    }

    public String getCourse() {
        return course;
    }
}
