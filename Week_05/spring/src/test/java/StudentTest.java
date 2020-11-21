import com.magaofei.Student;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author magaofei
 * @date 2020/11/21
 */
public class StudentTest {

    @Test
    public void test() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        Student student = (Student) applicationContext.getBean("student");
        System.out.println(student);

        Student student1 = (Student) applicationContext.getBean("student1");
        System.out.println(student1);
    }


}
