package com.ecnu.timeline.service;


import com.ecnu.timeline.domain.Message;

import java.text.SimpleDateFormat;
import java.util.*;

//仅为service测试打桩提供数据之用
//模拟数据库的操作和数据库的数据 repo_list 每次查询后的返回队列 return_list
public class DataSupplier {

    private List<Message> return_lists = new ArrayList<>();
    private List<Message> repo_lists = new ArrayList<>();

    private Integer i=0;//当前repo_lists最新一条数据的id
    private Integer j=0;//当前在repo中，但是没有被返回过的纪录数

    /*
    模拟数据库的插入，向repo_lists插入1条message，并返回该消息
     */
    public Message saveOneMessage(){
            Long cur = new Date().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            i++;
            j++;
            Message message = new Message.MessageBuilder()
                    .id(i)
                    .name("Wangzh")
                    .content("TestMessage" + i)
                    .time(cur)
                    .str_time(sdf.format(cur))
                    .image("images/universe.png")
                    .build_with_id();
            repo_lists.add(message);
            return message;
    }
    public void clear(){
        i=0;
        repo_lists = null;
        return_lists = null;
    }
    public List<Message> getRepo_lists(){
        return repo_lists;
    }
    public List<Message> getReturn_lists(){
        return return_lists;
    }
    public Integer getMaxID(){
        if (i!=0)return i;
        else
            return null;
    }
/*
    public Integer getNewRecordNum(){
        return j;
    }
*/

    public List<Message> findByIdBetweenOrderByIdDesc(Integer start,Integer end){
        if (start<=0||end<=0)return new ArrayList<>();
        int start_index = start-1;
        int end_index = end-1;
        return_lists=repo_lists.subList(start_index,end_index+1);//sublist的最后一个id是不加进去的
        Collections.reverse(return_lists);
        return return_lists;
    }

    public List<Message> findByIdGreaterThanOrderByIdDesc(Integer cur_max){
        return_lists=repo_lists.subList(cur_max,repo_lists.size());
        Collections.reverse(return_lists);
        return return_lists;
    }


}

