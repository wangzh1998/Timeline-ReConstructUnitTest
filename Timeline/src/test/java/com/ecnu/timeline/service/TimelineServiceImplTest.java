package com.ecnu.timeline.service;

import com.ecnu.timeline.domain.Message;
import com.ecnu.timeline.repository.MessageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
//@ExtendWith(SpringExtension.class)
// service当中维护着全局变量，每个方法都可能更改这张，这种情况下，各个方法以及对应的测试无法做到完全独立吧？
// 通过加一个清空的函数，每次都把全局变量置成初始状态，从而保证各个测试方法的独立
class TimelineServiceImplTest {

    //@Autowired
    //MessageRepository testRepository;

    //service依赖的内容都用@MockBean
    @MockBean
    MessageRepository repository;

    @MockBean
    TransformService transformService;//忘记这个MOCK对象了，一直报空浮点异常……

    //service本身仍然自动注入就可以了
    //@InjectMocks
    @Autowired
    TimelineService service;

    DataSupplier dataSupplier;
    SimpleDateFormat sdf;

    List<Message> return_lists;//纪录调用service中函数的返回值

    @BeforeEach
    void setUp() {
        //MockitoAnnotations.initMocks(this);
        dataSupplier = new DataSupplier();
        //repo_lists = new ArrayList<>();
        return_lists = new ArrayList<>();
        sdf = new SimpleDateFormat();
        service.flush();

    }

    @AfterEach
    void tearDown() {
        dataSupplier.clear();
        //repo_lists = null;
        return_lists = null;
    }


    @Test
    void should_get_newRecorNum_0_when_getNewRecordNum_before_insert(){
        //before 没有insert()时
        assertEquals(0, service.getNewRecordNum(), "插入之前，新纪录数为0");
    }
    @Test
    void should_get_newRecorNum_1_when_getNewRecordNum_after_insert() {
        // 插入之后，返回给前端的新纪录数是否为1

        //打桩
        Mockito.when(repository.save(any())).thenReturn(dataSupplier.saveOneMessage());//answer的返回类型要和save()的返回类型一致？否则会报错？

        service.insert();//前面的打桩是为了这里，虽然本身service.getNewRecordNum里面没有对mock对象的方法调用
        //after 调用被测方法后
        assertEquals(1, service.getNewRecordNum(), "插入之后，新纪录数应该更新为1");
    }


    @Test
    void should_return_null_when_insert_without_init() {
        //打桩
        Mockito.when(repository.getMaxID()).thenReturn(dataSupplier.getMaxID());
        Mockito.when(repository.save(any())).thenReturn(dataSupplier.saveOneMessage());//answer的返回类型要和save()的返回类型一致？否则会报错？
        doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里

        //调用被测方法
        return_lists = service.insert();

        //验证
        verify(repository, times(1)).getMaxID();
        verify(repository, times(1)).save(any());
        verify(transformService, times(1)).transformListsTime(any());
        assertEquals(0, return_lists.size(), "仅仅插入，返回给前端的列表应该为空");

    }

    @Test
    void should_return_5_message_when_insert_after_init() {

        //先初始化6条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //打桩
        Mockito.when(repository.getMaxID()).thenReturn(dataSupplier.getMaxID());
        Mockito.when(repository.save(any())).thenReturn(dataSupplier.saveOneMessage());//answer的返回类型要和save()的返回类型一致？否则会报错？
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(),anyInt())).thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(1,5));
        doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里

        return_lists = service.init();
        assertEquals(5, return_lists.size(), "在初始化时返回给前端的列表长度为5");
                // () -> assertEquals("TestMessage1", repo_lists.get(0).getContent(),"消息内容应该为TestMessage1")
                //此处不验证时间，因为有专门的时间转换工具的测试
        //调用被测方法
        return_lists = service.insert();
        //验证
        verify(repository, times(1)).save(any());
        assertEquals(5, return_lists.size(), "在初始化之后插入，返回给前端的列表应该内容不变，还是初始化的内容");
                // () -> assertEquals("TestMessage1", repo_lists.get(0).getContent(),"消息内容应该为TestMessage1")
                //此处不验证时间，因为有专门的时间转换工具的测试

    }


    @Test
    void should_return_5_message_when_init() {
        //作为初始始化，先向数据库列表repo_lists当中插入6条数据，然后初始化函数中从这6条数据中取出最新的5条
        //插入的时候不要写循环
        //验证:
        //1.repo_lists中数据有6条
        //2.return_lists返回最新的5条
        //3.return_lists返回的数据id倒序排列

        //先初始化6条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //打桩
        Mockito.when(repository.getMaxID()).thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(),anyInt())).thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(2,6));
        doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里
        //doAnswer(trans_answer).when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里
        //此处的answer没有任何返回，和transformListsTime的返回类型相匹配


        //调用被测方法
        return_lists = service.init();//执行初始化，不能忘记了

        //验证
        verify(repository, times(1)).getMaxID();
        verify(repository, times(1)).findByIdBetweenOrderByIdDesc(2, 6);
        verify(transformService, times(1)).transformListsTime(any());

        assertAll(
                () -> assertNotNull(return_lists),
                ()->assertEquals(5,return_lists.size(),"返回最新的数据lists的长度应该是5"),
                //验证消息id 倒序
                () -> assertEquals(6, return_lists.get(0).getId(), "消息应为6"),
                () -> assertEquals(5, return_lists.get(1).getId(), "消息应为5"),
                () -> assertEquals(4, return_lists.get(2).getId(), "消息应为4"),
                () -> assertEquals(3, return_lists.get(3).getId(), "消息应为3"),
                () -> assertEquals(2, return_lists.get(4).getId(), "消息应为2")
        );

    }
    @Test
    void should_return_0_message_when_init_but_there_is_no_message_in_db() {
        //作为初始始化，先向数据库列表repo_lists当中插入6条数据，然后初始化函数中从这6条数据中取出最新的5条
        //插入的时候不要写循环
        //验证:
        //1.repo_lists中数据有6条
        //2.return_lists返回最新的5条
        //3.return_lists返回的数据id倒序排列

        //无初始化

        //打桩
        Mockito.when(repository.getMaxID()).thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(),anyInt())).thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(0,0));
        doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里
        //doAnswer(trans_answer).when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里
        //此处的answer没有任何返回，和transformListsTime的返回类型相匹配


        //调用被测方法
        return_lists = service.init();//执行初始化，不能忘记了

        //验证
        verify(repository, times(1)).getMaxID();
        verify(repository, times(1)).findByIdBetweenOrderByIdDesc(anyInt(), anyInt());
        verify(transformService, times(1)).transformListsTime(any());

       assertEquals(0,return_lists.size());
    }

    @Test
    void should_return_3_message_when_init_but_there_is_only_3_message_in_db() {
        //先初始化3条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //打桩
        Mockito.when(repository.getMaxID()).thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(),anyInt())).thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(1,3));
        doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里
        //doAnswer(trans_answer).when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里
        //此处的answer没有任何返回，和transformListsTime的返回类型相匹配


        //调用被测方法
        return_lists = service.init();//执行初始化，不能忘记了

        //验证
        verify(repository, times(1)).getMaxID();
        verify(repository, times(1)).findByIdBetweenOrderByIdDesc(anyInt(),anyInt());
        verify(transformService, times(1)).transformListsTime(any());

        assertAll(
                () -> assertNotNull(return_lists),
                ()->assertEquals(3,return_lists.size(),"返回最新的数据lists的长度应该是5"),
                //验证消息id 倒序
                () -> assertEquals(3, return_lists.get(0).getId(), "消息应为3"),
                () -> assertEquals(2, return_lists.get(1).getId(), "消息应为2"),
                () -> assertEquals(1, return_lists.get(2).getId(), "消息应为1")
        );

    }

    @Test
    void should_get_5_message_whent_init_and_then_update() {
        //首先插入6条数据，然后init()，返回消息2-6
        //立刻进行更新 检测:
        //1.新消息条数为0
        //2.return_lists不变

        //先初始化6条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //stub 打桩
        Mockito.when(repository.getMaxID()).thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(), anyInt()))
                .thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(2,6));
        //Mockito.when(repository.save(any())).thenReturn(dataSupplier.saveOneMessage());
        Mockito.when(repository.findByIdGreaterThanOrderByIdDesc(anyInt()))
                .thenReturn(dataSupplier.findByIdGreaterThanOrderByIdDesc(6));
        Mockito.doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里


        //check 1,2
        return_lists = service.init();
        assertAll(
                () -> assertEquals(0, service.getNewRecordNum(), "新消息数为0"),
                () -> assertNotNull(return_lists),
                () -> assertEquals(5, return_lists.size()),
                () -> assertEquals(6, return_lists.get(0).getId(), "消息应为6"),
                () -> assertEquals(5, return_lists.get(1).getId(), "消息应为5"),
                () -> assertEquals(4, return_lists.get(2).getId(), "消息应为4"),
                () -> assertEquals(3, return_lists.get(3).getId(), "消息应为3"),
                () -> assertEquals(2, return_lists.get(4).getId(), "消息应为2")
        );
        return_lists = service.update();
        assertAll(
                () -> assertEquals(0, service.getNewRecordNum(), "新消息数为0"),
                () -> assertNotNull(return_lists),
                () -> assertEquals(5, return_lists.size()),
                () -> assertEquals(6, return_lists.get(0).getId(), "消息应为6"),
                () -> assertEquals(5, return_lists.get(1).getId(), "消息应为5"),
                () -> assertEquals(4, return_lists.get(2).getId(), "消息应为4"),
                () -> assertEquals(3, return_lists.get(3).getId(), "消息应为3"),
                () -> assertEquals(2, return_lists.get(4).getId(), "消息应为2")
        );

        verify(transformService, times(2)).transformListsTime(any());
    }

    @Test
    void should_get_all_messages_in_db_when_update_without_init(){
        //先初始化6条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //stub 打桩
        Mockito.when(repository.getMaxID()).thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        //Mockito.when(repository.save(any())).thenReturn(dataSupplier.saveOneMessage());
        Mockito.when(repository.findByIdGreaterThanOrderByIdDesc(anyInt()))
                .thenReturn(dataSupplier.findByIdGreaterThanOrderByIdDesc(0));
        Mockito.doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里

        return_lists = service.update();
        assertAll(
                () -> assertEquals(0, service.getNewRecordNum(), "新消息数为0"),
                () -> assertNotNull(return_lists,"不经过初始化，直接update(),会从数据库中读取全部消息进行显示"),
                () -> assertEquals(6, return_lists.size()),
                () -> assertEquals(6, return_lists.get(0).getId(), "消息应为6"),
                () -> assertEquals(5, return_lists.get(1).getId(), "消息应为5"),
                () -> assertEquals(4, return_lists.get(2).getId(), "消息应为4"),
                () -> assertEquals(3, return_lists.get(3).getId(), "消息应为3"),
                () -> assertEquals(2, return_lists.get(4).getId(), "消息应为2"),
                () -> assertEquals(1, return_lists.get(5).getId(), "消息应为2")
        );


    }

    @Test
    void should_get_0_message_when_showmore_without_init(){
        //先初始化6条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //stub 打桩
        Mockito.when(repository.getMaxID()).thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        //Mockito.when(repository.save(any())).thenReturn(dataSupplier.saveOneMessage());
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(),anyInt()))
                .thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(0,0));
        Mockito.doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里

        return_lists = service.update();
        assertAll(
                () -> assertEquals(0, service.getNewRecordNum(), "新消息数为0"),
                () -> assertEquals(0,return_lists.size(),"不经过初始化，直接showmore，不会显示任何消息")
        );

    }

    @Test
    void should_get_6_message_when_init_insert_ant_then_update() {
        //插入一条数据insert()后  检测:
        //return_lists大小为6，第1条数据是最新插入的数据7

        //先初始化6条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //stub 打桩
        Mockito.when(repository.getMaxID())
                .thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        //save的打桩放在find的前面
        Mockito.when(repository.save(any())).thenReturn(dataSupplier.saveOneMessage());

        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(), anyInt()))
                .thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(2,6))
                .thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(7,7));
        Mockito.when(repository.findByIdGreaterThanOrderByIdDesc(anyInt()))
                .thenReturn(dataSupplier.findByIdGreaterThanOrderByIdDesc(6));
        Mockito.doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里


        //service call
        return_lists = service.init();
        return_lists = service.insert();
        return_lists = service.update();
        //check
        assertAll(
                () -> assertEquals(0, service.getNewRecordNum(), "新消息数为0"),
                () -> assertNotNull(return_lists),
                () -> assertEquals(6, return_lists.size()),
                () -> assertEquals(7, return_lists.get(0).getId(), "消息应为7"),
                () -> assertEquals(6, return_lists.get(1).getId(), "消息应为6"),
                () -> assertEquals(5, return_lists.get(2).getId(), "消息应为5"),
                () -> assertEquals(4, return_lists.get(3).getId(), "消息应为4"),
                () -> assertEquals(3, return_lists.get(4).getId(), "消息应为3"),
                () -> assertEquals(2, return_lists.get(5).getId(), "消息应为2")
        );

    }


    @Test
    void should_return_6_message_when_init_ant_then_showmore() {
        //先插入6条数据，然后init()，再来showmore
        //验证:
        //1.init()后的return_lists大小是5 消息是 6 5 4 3 2
        //2.showmore()后的return lists大小是6 消息是 6 5 4 3 2 1

        //先初始化6条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //stub 打桩
        Mockito.when(repository.getMaxID())
                .thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(), anyInt()))
                .thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(2,6))
                .thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(1,1));
        Mockito.doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里


        //service call
        return_lists = service.init();
        return_lists = service.showmore();
        //check
        assertAll(
                () -> assertEquals(0, service.getNewRecordNum(), "新消息数为0"),
                () -> assertNotNull(return_lists),
                () -> assertEquals(6, return_lists.size()),
                () -> assertEquals(6, return_lists.get(0).getId(), "消息应为6"),
                () -> assertEquals(5, return_lists.get(1).getId(), "消息应为5"),
                () -> assertEquals(4, return_lists.get(2).getId(), "消息应为4"),
                () -> assertEquals(3, return_lists.get(3).getId(), "消息应为3"),
                () -> assertEquals(2, return_lists.get(4).getId(), "消息应为2"),
                () -> assertEquals(1, return_lists.get(5).getId(), "消息应为1")
        );

    }

    @Test
    void should_return_5_message_when_init_ant_then_showmore_but_there_are_only_5_message_in_db() {
        //先插入5条数据，然后init()，再来showmore
        //验证:
        //1.init()后的return_lists大小是5 消息是 6 5 4 3 2
        //2.showmore()后的return lists大小是6 消息是 6 5 4 3 2 1

        //先初始化6条数据
        //初始化数据要放在打桩前面，不然打桩用到dataSupplier里面的lists，都是空的，会报异常
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();
        dataSupplier.saveOneMessage();

        //stub 打桩
        Mockito.when(repository.getMaxID())
                .thenReturn(dataSupplier.getMaxID());//数据的最大id和数据库列表的长度一致
        Mockito.when(repository.findByIdBetweenOrderByIdDesc(anyInt(), anyInt()))
                .thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(1,5))
                .thenReturn(dataSupplier.findByIdBetweenOrderByIdDesc(0,0));
        Mockito.doNothing().when(transformService).transformListsTime(return_lists);//transformService的方法写在when().后面，而不是括号里


        //service call
        return_lists = service.init();
        return_lists = service.showmore();
        //check
        assertAll(
                () -> assertEquals(0, service.getNewRecordNum(), "新消息数为0"),
                () -> assertNotNull(return_lists),
                () -> assertEquals(5, return_lists.size()),
                () -> assertEquals(5, return_lists.get(0).getId(), "消息应为5"),
                () -> assertEquals(4, return_lists.get(1).getId(), "消息应为4"),
                () -> assertEquals(3, return_lists.get(2).getId(), "消息应为3"),
                () -> assertEquals(2, return_lists.get(3).getId(), "消息应为2"),
                () -> assertEquals(1, return_lists.get(4).getId(), "消息应为1")
        );

    }
}







