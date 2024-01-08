package bg.sofia.uni.fmi.mjt.udemy.course.duration;

import bg.sofia.uni.fmi.mjt.udemy.course.Resource;

public record CourseDuration (int hours, int minutes) {
    public CourseDuration {
        if(hours<0 || hours>24){
            throw new IllegalArgumentException();
        }
        if(minutes<0 || minutes>60){
            throw new IllegalArgumentException();
        }
    }
    public static CourseDuration of(Resource[] content){
        int minutes = 0;
        for(Resource resource : content){
            minutes+=resource.getDuration().minutes();
        }
        int hours = 0;
        while(minutes>=60){
            hours++;
            minutes-=60;
        }
        return new CourseDuration(hours,minutes);
    }
}
