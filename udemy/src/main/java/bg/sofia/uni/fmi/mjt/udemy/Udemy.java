package bg.sofia.uni.fmi.mjt.udemy;

import bg.sofia.uni.fmi.mjt.udemy.account.Account;
import bg.sofia.uni.fmi.mjt.udemy.course.Category;
import bg.sofia.uni.fmi.mjt.udemy.course.Course;
import bg.sofia.uni.fmi.mjt.udemy.exception.AccountNotFoundException;
import bg.sofia.uni.fmi.mjt.udemy.exception.CourseNotFoundException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Udemy implements LearningPlatform{
    Account[] accounts;
    Course[] courses;

    public Udemy(Account[] accounts, Course[] courses) {
        this.accounts = accounts;
        this.courses = courses;
    }

    @Override
    public Course findByName(String name) throws CourseNotFoundException {
        if(name ==  null || name.isEmpty()){
            throw new IllegalArgumentException();
        }
        for(Course course : courses){
            if(course.getName().equals(name)){
                return course;
            }
        }
        throw new CourseNotFoundException();
    }

    @Override
    public Course[] findByKeyword(String keyword) {
        if(keyword ==  null || keyword.isEmpty()){
            throw new IllegalArgumentException();
        }

        if(!keyword.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException();
        }

        int count = 0;
        for(Course course : courses){
            if(course.getName().contains(keyword) || course.getDescription().contains(keyword)){
                count++;
            }
        }

        if(count == 0){
            return new Course[0];
        }

        Course[] res = new Course[count];
        int i=0;
        for(Course course : courses){
            if(course.getName().contains(keyword) || course.getDescription().contains(keyword)){
                res[i] = course;
                i++;
            }
        }
        return res;
    }

    @Override
    public Course[] getAllCoursesByCategory(Category category) {
        if(category ==  null){
            throw new IllegalArgumentException();
        }

        int count = 0;
        for(Course course : courses){
            if(course.getCategory() == category){
                count++;
            }
        }

        Course[] res = new Course[count];
        int i=0;
        for(Course course : courses){
            if(course.getCategory() == category){
                res[i] = course;
                i++;
            }
        }
        return res;
    }

    @Override
    public Account getAccount(String name) throws AccountNotFoundException {
        if(name ==  null || name.isEmpty()){
            throw new IllegalArgumentException();
        }
        for(Account account:accounts){
            if(account.getUsername().equals(name)){
                return account;
            }
        }
        throw new AccountNotFoundException();
    }

    @Override
    public Course getLongestCourse() {
        if(courses.length == 0) {
            return null;
        }
        Course longest = courses[0];
        for(Course course : courses){
            if(course.getTotalTime().hours() > longest.getTotalTime().hours()){
                longest = course;
            }else if (course.getTotalTime().hours() == longest.getTotalTime().hours() && course.getTotalTime().minutes() > longest.getTotalTime().minutes()){
                longest = course;
            }
        }
        return longest;
    }

    @Override
    public Course getCheapestByCategory(Category category) {
        if(category ==  null){
            throw new IllegalArgumentException();
        }

        Course cheapest = null;
        for(Course course : courses){
            if(course.getCategory().equals(category) && (cheapest == null || cheapest.getPrice() > course.getPrice())){
                cheapest = course;
            }
        }
        return cheapest;
    }
}
