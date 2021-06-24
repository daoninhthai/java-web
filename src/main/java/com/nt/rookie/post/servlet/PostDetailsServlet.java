package com.nt.rookie.post.servlet;

import com.nt.rookie.post.data.DaoFactory;
import com.nt.rookie.post.data.PostDao;
import com.nt.rookie.post.domain.Post;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class PostDetailsServlet extends HttpServlet {
    PostDao postDao = DaoFactory.getPostDao();

    public void doGet (HttpServletRequest request , HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        int id = Integer.parseInt(request.getParameter("id"));
        System.out.println("PostDetailsServlet::"+id);
        Post post =postDao.find(id);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<h2>"+post.getTitle()+"</h2>");
        out.println(post.getContent());
        out.println("<a href='/searchpost'>View All Post</a>");
        out.println("</body>");
        out.println("</html>");


    }
}
