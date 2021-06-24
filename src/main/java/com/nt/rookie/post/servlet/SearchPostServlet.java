package com.nt.rookie.post.servlet;

import com.nt.rookie.post.common.BaseConstants;
import com.nt.rookie.post.common.DateUtil;
import com.nt.rookie.post.data.DaoFactory;
import com.nt.rookie.post.data.PostDao;
import com.nt.rookie.post.domain.Post;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

public class SearchPostServlet extends HttpServlet {
    PostDao postDao = DaoFactory.getPostDao();
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String searchTerm = request.getParameter("searchTerm");
        System.out.println("SearchPostServlet::"+searchTerm);
        List<Post> posts =postDao.search((searchTerm));
        //response
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter out = response.getWriter();
        out.print("<html>");
        out.print("<body>");
        for(Post post:posts){
            out.print("<artical>");
            out.print("<a href='/postdetails?id="+post.getId()+"'>");
            out.print("<h2 class='post-preview'>"+post.getTitle()+"</h2>");
            out.print("<h3 class='post-subtitle'>"+post.getDescription()+"</h3>");
            out.print("<p>Posted by"+post.getAuthor().getFirstName()
                    +" "+post.getAuthor().getLastName()
                    +" on "
                    + DateUtil.format(post.getDate(), BaseConstants.PATTERM_MONTH_DAY_YEAR)
                    +".8 mins read");

            out.print("</a>");
            //btn edit
            out.println("<form method ='GET' action='/postedit'>");
            out.println("<input type='hidden' name ='id' value='"+post.getId()+"'/>");
            out.println("<input type='submit' value='Edit'/>");
            out.println("</form>");
            //btn edit end here
            out.print("</artical>");
            out.print("<hr>");
        }
        out.println("<a href='/searchpost'>View all Posts</a>");
        out.print("</body>");
        out.println("</html");

    }
}
