package com.nt.rookie.post.servlet;

import com.nt.rookie.post.data.DaoFactory;
import com.nt.rookie.post.data.PostDao;
import com.nt.rookie.post.domain.Post;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
@SuppressWarnings("serial")
public class PostEditServlet extends HttpServlet {
    PostDao postDao = DaoFactory.getPostDao();

    public void doGet (HttpServletRequest request , HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        int id = Integer.parseInt(request.getParameter("id"));
        System.out.println("PostEditServlet::"+id);
        Post post =postDao.find(id);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<form id='editform' method='POST' action=''>");
        out.println("<input type='text' name='newTitle' value='"
                +post.getTitle()
                +"'style='width:500px'/><br>");
        out.println("<input type='text' name='newDescription' value='"
                +post.getDescription()
                +"'style='width:500px'/><br>");
        out.println("<textarea form='editform' name='newContent' rows='10' style='width:500px'>"
                + post.getContent()
                + "</textarea></br>");
        out.println("<input type='hidden' name='id' value='"+post.getId()+"'/>");
        out.println("<input type='submit' value='Update'/>");

        out.println("</form>");

        out.println("<a href='/searchpost'>View All Post</a>");
        out.println("</body>");
        out.println("</html>");

}
    public void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException{
        request.setCharacterEncoding("UTF-8");
        int id = Integer.parseInt(request.getParameter("id"));
        String newTitle = request.getParameter(("newTitle"));
        String newDescription = request.getParameter("newDescription");
        String newContent =request.getParameter("newContent");
        System.out.println("PostEditServlet::"+id);

        //update
        Post newPosts = new Post();
        newPosts.setId(id);
        newPosts.setTitle(newTitle);
        newPosts.setDescription(newDescription);
        newPosts.setContent(newContent);
        postDao.update(newPosts);
        response.sendRedirect("/postdetails?id="+id);
    }
}
