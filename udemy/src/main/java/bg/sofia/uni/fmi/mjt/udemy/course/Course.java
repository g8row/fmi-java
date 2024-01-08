package bg.sofia.uni.fmi.mjt.udemy.course;

import bg.sofia.uni.fmi.mjt.udemy.course.duration.CourseDuration;
import bg.sofia.uni.fmi.mjt.udemy.exception.ResourceNotFoundException;

public class Course implements Completable,Purchasable{
    String name;
    String description;
    double price;
    Resource[] content;
    Category category;
    CourseDuration totalTime;
    boolean purchased;

    public Course(String name, String description, double price, Resource[] content, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.content = content;
        this.category = category;
        this.totalTime = CourseDuration.of(content);
    }

    /**
     * Returns the name of the course.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the course.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the price of the course.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns the category of the course.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Returns the content of the course.
     */
    public Resource[] getContent() {
        return content;
    }

    /**
     * Returns the total duration of the course.
     */
    public CourseDuration getTotalTime() {
        return totalTime;
    }

    /**
     * Completes a resource from the course.
     *
     * @param resourceToComplete the resource which will be completed.
     * @throws IllegalArgumentException if resourceToComplete is null.
     * @throws ResourceNotFoundException if the resource could not be found in the course.
     */
    public void completeResource(Resource resourceToComplete) throws ResourceNotFoundException {
        if(resourceToComplete == null){
            throw new IllegalArgumentException();
        }
        for(Resource resource : content){
            if(resource.equals(resourceToComplete)){
                resource.complete();
                return;
            }
        }
        throw new ResourceNotFoundException();
    }

    @Override
    public boolean isCompleted() {
        for(Resource resource : content){
            if(!resource.isCompleted()){
                return false;
            }
        }
        return true;
    }

    @Override
    public int getCompletionPercentage() {
        int countCompleted = 0;
        for(Resource resource : content){
            if(resource.isCompleted()){
                countCompleted++;
            } rge
        }
        return (int) Math.round((double) countCompleted * 100 / content.length);
    }

    @Override
    public void purchase() {
        purchased = true;
    }

    @Override
    public boolean isPurchased() {
        return purchased;
    }
}
