package com.nt.rookie.post.data;

//This is not thread-safe and is used as a quick implementation for learning web development.
//In a production system you would use Spring, Hibernate or another framework to do this and more! 
public class DaoFactory {

    private static PostDao postDao;

    public static PostDao getPostDao() {
        if (postDao == null) {
            postDao = new PostDao();
        }
        return postDao;
    }
}
