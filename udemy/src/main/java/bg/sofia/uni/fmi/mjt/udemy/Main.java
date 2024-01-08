package bg.sofia.uni.fmi.mjt.udemy;

import bg.sofia.uni.fmi.mjt.udemy.LearningPlatform;
import bg.sofia.uni.fmi.mjt.udemy.Udemy;
import bg.sofia.uni.fmi.mjt.udemy.account.Account;
import bg.sofia.uni.fmi.mjt.udemy.account.BusinessAccount;
import bg.sofia.uni.fmi.mjt.udemy.account.EducationalAccount;
import bg.sofia.uni.fmi.mjt.udemy.account.StandardAccount;
import bg.sofia.uni.fmi.mjt.udemy.course.Category;
import bg.sofia.uni.fmi.mjt.udemy.course.Course;
import bg.sofia.uni.fmi.mjt.udemy.course.Resource;
import bg.sofia.uni.fmi.mjt.udemy.course.duration.ResourceDuration;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        LearningPlatform udemy = new Udemy(
                new Account[]{
                        new BusinessAccount("Jhon", 100, new Category[]{Category.BUSINESS, Category.FINANCE}),
                        new BusinessAccount("Jhon2", 100, new Category[]{Category.BUSINESS, Category.FINANCE}),
                        new StandardAccount("Bob", 100),
                        new EducationalAccount("Alex", 100)
                },
                new Course[]{
                        new Course("Business course 101", "", 10, new Resource[]{
                                new Resource("Lesson 1", new ResourceDuration(40)),
                                new Resource("Lesson 2", new ResourceDuration(40)),
                                new Resource("Lesson 3", new ResourceDuration(40)),
//                                new Resource("Lesson 4", new ResourceDuration(40)),
//                                new Resource("Lesson 5", new ResourceDuration(40)),
//                                new Resource("Lesson 6", new ResourceDuration(40)),
                        }, Category.BUSINESS),
                        new Course("Business course 102", "", 10, new Resource[]{
                                new Resource("Lesson 1", new ResourceDuration(40)),
                                new Resource("Lesson 2", new ResourceDuration(40)),
                                new Resource("Lesson 3", new ResourceDuration(40)),}
//                                new Resource("Lesson 4", new ResourceDuration(40)),
//                                new Resource("Lesson 5", new ResourceDuration(40)),
//                                new Resource("Lesson 6", new ResourceDuration(40)),
                                , Category.BUSINESS),
                        new Course("Business coursee 103", "", 10, new Resource[]{}, Category.BUSINESS),
                        new Course("Business coursee 104", "", 10, new Resource[]{}, Category.BUSINESS),
                        new Course("Business course 105", "", 10, new Resource[]{}, Category.BUSINESS),
                        new Course("Business course 106", "", 10, new Resource[]{}, Category.BUSINESS)
                }
        );



        try {
            udemy.getAccount("Jhon").buyCourse(udemy.findByName("Business course 101"));
            udemy.getAccount("Jhon").buyCourse(udemy.findByName("Business course 102"));
//            udemy.getAccount("Alex").buyCourse(udemy.findByName("Business course 103"));
//            udemy.getAccount("Alex").buyCourse(udemy.findByName("Business course 104"));
//            udemy.getAccount("Alex").buyCourse(udemy.findByName("Business course 105"));

            udemy.getAccount("Jhon")
                    .completeResourcesFromCourse(udemy.findByName("Business course 101"),
                            new Resource[]{
                                    new Resource("Lesson 1", new ResourceDuration(40)),
                                    new Resource("Lesson 2", new ResourceDuration(40)),
                            });
            udemy.getAccount("Jhon")
                    .completeResourcesFromCourse(udemy.findByName("Business course 102"),
                            new Resource[]{
                                    new Resource("Lesson 1", new ResourceDuration(40)),
                                    new Resource("Lesson 2", new ResourceDuration(40)),
                            });


            System.out.println((int) Math.round((double) 2 * 100 / 3));
            System.out.println(udemy.getAccount("Jhon").getLeastCompletedCourse().getCompletionPercentage());
            Course[] arr = udemy.findByKeyword("ee");

            //udemy.getAccount("Alex").completeCourse(udemy.findByName("Business course 101"),5);
//            udemy.getAccount("Alex").completeCour se(udemy.findByName("Business course 102"),5);
//            udemy.getAccount("Alex").completeCourse(udemy.findByName("Business course 103"),5);
//            udemy.getAccount("Alex").completeCourse(udemy.findByName("Business course 104"),5);
//            udemy.getAccount("Alex").completeCourse(udemy.findByName("Business course 105"),5);


//            udemy.getAccount("Alex").buyCourse(udemy.findByName("Business course 106"));
//
//            udemy.getAccount("Bob").buyCourse(udemy.findByName("Business course 101"));
//
//            udemy.getAccount("Jhon").buyCourse(udemy.findByName("Business course 101"));
//
//            udemy.getAccount("Alex").addToBalance(300);

            Course course = udemy.getLongestCourse();
            System.out.println(udemy.getAccount("Alex").getBalance());
            System.out.println(udemy.getAccount("Bob").getBalance());
            System.out.println(udemy.getAccount("Jhon").getBalance());

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
