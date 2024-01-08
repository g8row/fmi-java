package bg.sofia.uni.fmi.mjt.udemy.account;

import bg.sofia.uni.fmi.mjt.udemy.account.type.AccountType;
import bg.sofia.uni.fmi.mjt.udemy.course.Course;
import bg.sofia.uni.fmi.mjt.udemy.course.Resource;
import bg.sofia.uni.fmi.mjt.udemy.exception.*;

public abstract class AccountBase implements Account{
    String username;
    AccountType accountType;
    double balance;
    Course[] courses;
    int capacity = 100;
    double[] grades;

    public AccountBase(String username,  double balance, AccountType accountType) {
        this.username = username;
        this.accountType = accountType;
        this.balance = balance;
        courses = new Course[0];
        grades = new double[capacity];
    }

    public AccountBase(String username, double balance) {
        this(username,balance,AccountType.STANDARD);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void addToBalance(double amount) {
        if(amount <0){
            throw new IllegalArgumentException();
        }
        balance+=amount;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public void buyCourse(Course course) throws InsufficientBalanceException, CourseAlreadyPurchasedException, MaxCourseCapacityReachedException {
        for(Course crs: courses){
            if(crs.getName().equals(course.getName())){
                throw new CourseAlreadyPurchasedException();
            }
        }
        if(balance<course.getPrice()) {
            throw new InsufficientBalanceException();
        }
        if(courses.length >= capacity){
            throw new MaxCourseCapacityReachedException();
        }
        if(accountType == AccountType.EDUCATION){
            if(courses.length >= 5){
                double avg = 0;
                for(int i=1;i<=5;i++){
                    avg += grades[courses.length - i];
                }
                avg = avg/5.0;
                if(avg >= 4.5) {
                    balance -= course.getPrice() - (course.getPrice() * accountType.getDiscount());
                }else {
                    balance -= course.getPrice();
                }
            }else{
                balance -= course.getPrice();
            }
        } else if(accountType == AccountType.BUSINESS){
            balance -= course.getPrice() - (course.getPrice() * accountType.getDiscount());
        }else{
            balance -= course.getPrice();
        }
        course.purchase();
        Course[] temp = new Course[courses.length+1];
        for(int i=0;i<courses.length;i++){
            temp[i]=courses[i];
        }
        temp[temp.length-1] = course;
        courses = temp;
    }

    @Override
    public void completeResourcesFromCourse(Course course, Resource[] resourcesToComplete) throws CourseNotPurchasedException, ResourceNotFoundException {
        Course temp = null;
        for(Course crs: courses){
            if(crs.getName().equals(course.getName())){
                temp = crs;
            }
        }
        if(temp == null){
            throw new CourseNotPurchasedException();
        }
        for(Resource resourceArg: resourcesToComplete){
             boolean changed = false;
             for(Resource rsrc: temp.getContent()){
                 if(rsrc.getName().equals(resourceArg.getName())){
                     rsrc.complete();
                     changed = true;
                     break;
                 }
             }
             if(!changed){
                 throw new ResourceNotFoundException();
             }
        }

    }

    @Override
    public void completeCourse(Course course, double grade) throws CourseNotPurchasedException, CourseNotCompletedException {
        Course temp = null;
        int index = 0;
        for(Course crs: courses){
            if(crs.getName().equals(course.getName())){
                temp = course;
                break;
            }
            index++;
        }
        if(temp == null){
            throw new CourseNotPurchasedException();
        }
        if(!temp.isCompleted()){
            throw new CourseNotCompletedException();
        }
        if(grade<2 || grade>6){
            throw new IllegalArgumentException();
        }
        grades[index] = grade;
    }

    @Override
    public Course getLeastCompletedCourse() {
        Course least = null;
        for(Course course: courses){
            if(least == null || course.getCompletionPercentage()< least.getCompletionPercentage()){
                least = course;
            }
        }
        return least;
    }
}
