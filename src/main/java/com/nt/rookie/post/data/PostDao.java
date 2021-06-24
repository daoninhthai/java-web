package com.nt.rookie.post.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.nt.rookie.post.domain.Author;
import com.nt.rookie.post.domain.Post;

public class PostDao extends AbstractDao {

    public PostDao() {
    }

    private List<Post> buildPost(ResultSet results) throws SQLException {
        Set<Post> posts = new HashSet<Post>();

        while (results.next()) {

            Author author = new Author();
            author.setUsername(results.getString("author"));
            author.setPassword(results.getString("password"));
            author.setFirstName(results.getString("first_name"));
            author.setLastName(results.getString("last_name"));
            author.setEmail(results.getString("email"));
            author.setAdded(results.getDate("birthdate"));

            Post post = new Post();
            post.setId(results.getInt("id"));
            post.setTitle(results.getString("title"));
            post.setDescription(results.getString("description"));
            post.setContent(results.getString("content"));
            post.setDate(results.getDate("date"));
            post.setAuthor(author);

            posts.add(post);
        }

        List<Post> list = new ArrayList<>();
        list.addAll(posts);
        return list;
    }

    public List<Post> search(String searchTerm) {
        List<Post> posts = new ArrayList<>();
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT posts.id, posts.author, posts.title, posts.description, posts.content, posts.date,"
                + "authors.username, authors.password, authors.first_name, authors.last_name, authors.email, authors.birthdate, authors.added "
                + "FROM posts "
                + "INNER JOIN authors ON posts.author = authors.username "
            );

            if (null != searchTerm) {
                sql.append("WHERE LOWER(posts.title) LIKE ? ");
                // Other filter go here
            }

            System.out.println(sql.toString());

            Connection conn = DriverManager.getConnection(JDBC_CONNECT,"","");
            PreparedStatement stm = conn.prepareStatement(sql.toString());
            if (null != searchTerm) {
                stm.setString(1, "%" + searchTerm.toLowerCase() + "%");
            }
            // Other parameter go here


            ResultSet results = stm.executeQuery();
            posts = buildPost(results);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    public Post find(int id) {
        List<Post> posts = new ArrayList<>();

        try{
            StringBuilder sql = new StringBuilder(
                "SELECT posts.id, posts.author, posts.title, posts.description, posts.content, posts.date,"
                + "authors.username, authors.password, authors.first_name, authors.last_name, authors.email, authors.birthdate, authors.added "
                + "FROM posts "
                + "INNER JOIN authors ON posts.author = authors.username "
                + "WHERE posts.id = ? "
            );

            System.out.println(sql.toString());

            Connection conn = DriverManager.getConnection(JDBC_CONNECT,"","");
            PreparedStatement stm = conn.prepareStatement(sql.toString());
            stm.setInt(1, id);

            ResultSet results = stm.executeQuery();
            posts = buildPost(results);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (posts.isEmpty()) {
            return null;
        } else {
            return posts.get(0);
        }
    }

    public List<Post> find(List<Integer> ids) {

        try {

            StringBuilder sql = new StringBuilder(
                "SELECT post.* FROM posts " +
                "WHERE posts.id = ? "
            ).append("WHERE post.id IN ('" + StringUtils.join(ids, "','") + "')");

            System.out.println(sql.toString());

            Connection conn = DriverManager.getConnection(JDBC_CONNECT, "", "");
            PreparedStatement stm = conn.prepareStatement(sql.toString());

            ResultSet results = stm.executeQuery();
            return buildPost(results);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean update(Post newPost) {
        try {

            StringBuilder sql = new StringBuilder("UPDATE posts SET title= ? , description= ? , content= ? , date= ? WHERE id= ?;");

            System.out.println(sql.toString());

            Connection conn = DriverManager.getConnection(JDBC_CONNECT, "", "");
            PreparedStatement prepStm = conn.prepareStatement(sql.toString());
            int index = 1;
            prepStm.setString(index++, newPost.getTitle());
            prepStm.setString(index++, newPost.getDescription());
            prepStm.setString(index++, newPost.getContent());
            prepStm.setDate(index++, new java.sql.Date(new Date().getTime()));
            prepStm.setInt(index++, newPost.getId());

            prepStm.execute();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
