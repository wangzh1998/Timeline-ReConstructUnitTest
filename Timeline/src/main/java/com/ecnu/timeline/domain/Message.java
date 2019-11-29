package com.ecnu.timeline.domain;

import com.mysql.cj.MessageBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.awt.*;
import java.util.Date;

@Entity
public class Message {

    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String content;
    private long time;
    private String str_time;//用于显示界面的时间转换
    private String image;//图片路径

    public Message(){}

    public Message(Integer id, String name, String content, long time, String str_time, String image) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.time = time;
        this.str_time = str_time;
        this.image = image;
    }


    public Message(String name, String content, long time, String str_time, String image) {
        this.name = name;
        this.content = content;
        this.time = time;
        this.str_time = str_time;
        this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public long getTime() {
        return time;
    }

    public String getImage() {
        return image;
    }

    public String getStr_time() {
        return str_time;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setStr_time(String str_time) {
        this.str_time = str_time;
    }

    public static class MessageBuilder{
        private Integer id;
        private String name;
        private String content;
        private long time;
        private String str_time;//用于显示界面的时间转换
        private String image;//图片路径

        public MessageBuilder id(Integer id){
            this.id = id;
            return this;
        }

        public MessageBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MessageBuilder content(String content) {
            this.content = content;
            return this;
        }

        public MessageBuilder time(long time) {
            this.time = time;
            return this;
        }

        public MessageBuilder str_time(String str_time) {
            this.str_time = str_time;
            return this;
        }

        public MessageBuilder image(String image) {
            this.image = image;
            return this;
        }

        public Message build(){
            return new Message(name,content,time,str_time,image);
        }
        public Message build_with_id(){
            return new Message(id,name,content,time,str_time,image);
        }
    }
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", time=" + time +
                ", str_time='" + str_time + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
