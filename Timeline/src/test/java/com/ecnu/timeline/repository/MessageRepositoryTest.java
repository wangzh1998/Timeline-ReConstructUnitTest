package com.ecnu.timeline.repository;

import com.ecnu.timeline.domain.Message;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@DirtiesContext
//@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageRepositoryTest {

    //@MockBean @Mock @InjectMocks 都不行
    @Autowired
    MessageRepository repository;

    //must !!
    @Autowired
    private EntityManager entityManager;


    /*
    虽然static不好，但是这里的测试都读取同样的数据,
    如果每个测试前都重新插入，完了再删除，会导致id的变更，于是一定要强制设定测试方法的执行顺序。
    而且这样还有一个问题，就是如果单独执行其中一个，一定会失败
    但是如果此处选择用static的方法 @BeforeAll和@AfterAll，会导致测试都被忽略了。可能是entitymanager不能是static吧
    */
    @BeforeEach
    void insert_3_message_before_each_test() {
        Long cur = new Date().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Message message1 = new Message.MessageBuilder()
                .name("Wangzh")
                .content("TestMessage" + 1)
                .time(cur)
                .str_time(sdf.format(cur))
                .image("images/universe.png")
                .build();
        Message message2 = new Message.MessageBuilder()
                .name("Wangzh")
                .content("TestMessage" + 2)
                .time(cur)
                .str_time(sdf.format(cur))
                .image("/images/universe.png")
                .build();
        Message message3 = new Message.MessageBuilder()
                .name("Wangzh")
                .content("TestMessage" + 3)
                .time(cur)
                .str_time(sdf.format(cur))
                .image("/images/universe.png")
                .build();
        /*repository.save(message1);
        repository.save(message2);
        repository.save(message3);*/
        //这里用的存储不是用repository的save()，而是entityManager的persist
        entityManager.persist(message1);
        entityManager.persist(message2);
        entityManager.persist(message3);
    }

    @AfterEach
    void clear_all_message_after_each_test() {
        /*repository.deleteAll();*/
        //同存储，此处清理也用到entityManager
        entityManager.clear();
    }


    @Test
    @Order(1)
    void should_return_3_when_get_max_id() {
        assertEquals(3, repository.getMaxID());
    }

    @Test
    @Order(2)
    void should_return_3_message_in_desc_when_find_by_id_greater_than_3() {
        List<Message> lists = repository.findByIdGreaterThanOrderByIdDesc(0);
        assertAll(
                () -> assertNotNull(lists),
                () -> assertEquals(3, lists.size()),
                () -> assertEquals(6, lists.get(0).getId()),
                () -> assertEquals("TestMessage3", lists.get(0).getContent()),
                () -> assertEquals(5, lists.get(1).getId()),
                () -> assertEquals("TestMessage2", lists.get(1).getContent()),
                () -> assertEquals(4, lists.get(2).getId()),
                () -> assertEquals("TestMessage1", lists.get(2).getContent())
        );
    }

    @Test
    @Order(3)
    void should_return_3_message_in_desc_when_find_by_id_between_7_and_9() {
        List<Message> lists = repository.findByIdBetweenOrderByIdDesc(7,9);
        assertAll(
                () -> assertNotNull(lists),
                () -> assertEquals(3, lists.size()),
                () -> assertEquals(9, lists.get(0).getId()),
                () -> assertEquals("TestMessage3", lists.get(0).getContent()),
                () -> assertEquals(8, lists.get(1).getId()),
                () -> assertEquals("TestMessage2", lists.get(1).getContent()),
                () -> assertEquals(7, lists.get(2).getId()),
                () -> assertEquals("TestMessage1", lists.get(2).getContent())
        );
    }
}


/*@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//JPA接口是否无法通过隔离外部依赖（DB）来完成测试？//stub mock 用repo_lists来模拟存储
class MessageRepositoryTest {

  //@MockBean @Mock @InjectMocks 都不行
    @Autowired
    MessageRepository repository ;

    Integer before_max_id;

    //should 建立一个实体对象
    Message message;




    @BeforeAll
    void beforeAll(){
        before_max_id = repository.getMaxID();
        Long cur = new Date().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Message message = new Message(before_max_id+1,"Wangzh","TestMessage"+1,cur,sdf.format(cur),"imagepath");
        Message message2 = new Message(before_max_id+2,"Wangzh","TestMessage"+2,cur,sdf.format(cur),"imagepath");
        repository.save(message);
        repository.save(message2);
    }
    @AfterAll
    void afterAll(){//做删除，则jpa维护的自增id和现在的最大id不同步 //这样两次测试只会成功一次
        repository.deleteById(before_max_id+1);
        repository.deleteById(before_max_id+2);
    }
    @BeforeEach
    void setUp(){

    }
    @AfterEach
    void tearUp(){
    }

    @Test
    void getMaxID(){
        assertEquals(before_max_id+2,repository.getMaxID());
    }
    @Test
    void findByIdGreaterThanOrderByIdDesc() {
        List<Message> lists = repository.findByIdGreaterThanOrderByIdDesc(before_max_id);
        assertAll(
                ()->assertNotNull(lists),
                ()->assertEquals(2,lists.size()),
                ()->assertEquals(before_max_id+2,lists.get(0).getId()),
                ()->assertEquals("TestMessage2",lists.get(0).getContent()),
                ()->assertEquals(before_max_id+1,lists.get(1).getId()),
                ()->assertEquals("TestMessage1",lists.get(1).getContent())
        );
    }
    @Test
    void findByIdBetweenOrderByIdDesc() {
        List<Message> lists = repository.findByIdBetweenOrderByIdDesc(before_max_id+1,before_max_id+2);
        assertAll(
                ()->assertNotNull(lists),
                ()->assertEquals(2,lists.size()),
                ()->assertEquals(before_max_id+2,lists.get(0).getId()),
                ()->assertEquals("TestMessage2",lists.get(0).getContent()),
                ()->assertEquals(before_max_id+1,lists.get(1).getId()),
                ()->assertEquals("TestMessage1",lists.get(1).getContent())
        );
    }


@BeforeEach
    void setUp() {

        Long cur = new Date().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Message message = new Message(1,"Wangzh","TestMessage"+1,cur,sdf.format(cur),"imagepath");
        Message message2 = new Message(2,"Wangzh","TestMessage"+2,cur,sdf.format(cur),"imagepath");
        repo_lists.add(message);
        repo_lists.add(message2);

        Answer<List<Message>> find_between_answer = new Answer<List<Message>>() {
            @Override
            public List<Message> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return_lists.clear();
                return_lists.addAll(repo_lists);
                return return_lists;
            }
        };
        Answer<List<Message>> find_greanter_answer = new Answer<List<Message>>() {
            @Override
            public List<Message> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return_lists.clear();
                return_lists.add(repo_lists.get(1));
                return return_lists;
            }
        };

        //stub
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(),anyInt())).thenAnswer(find_between_answer);
        //返回有两条纪录 1和2 的return lists
        Mockito.when(repository.findByIdGreaterThanOrderByIdDesc(anyInt())).thenAnswer(find_greanter_answer);
        //返回只有1条纪录 2 的return lists
        Mockito.when(repository.getMaxID()).thenReturn(2);//返回当前数据库中的最大id2
    }


    @AfterEach
    void tearDown() {
        repo_lists.clear();
        return_lists.clear();
    }

    @Test
    void getMaxID() {
        assertEquals(2,repository.getMaxID());
    }

    @Test
    void findByIdGreaterThanOrderByIdDesc() {
        List<Message> lists = repository.findByIdGreaterThanOrderByIdDesc(1);
        assertAll(
                ()->assertNotNull(lists),
                ()->assertEquals(1,lists.size()),
                ()->assertEquals(2,lists.get(0).getId()),
                ()->assertEquals("TestMessage2",lists.get(0).getContent())
        );
    }

    @Test
    void findByIdBetweenOrderByIdDesc() {
        List<Message> lists = repository.findByIdBetweenOrderByIdDesc(1,2);
        assertAll(
                ()->assertNotNull(lists),
                ()->assertEquals(2,lists.size()),
                ()->assertEquals(1,lists.get(0).getId()),
                ()->assertEquals("TestMessage1",lists.get(0).getContent()),
                ()->assertEquals(2,lists.get(1).getId()),
                ()->assertEquals("TestMessage2",lists.get(1).getContent())
        );
    }

}*/
