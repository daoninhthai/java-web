package com.nt.rookie.post.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@SuppressWarnings("serial")
public class HelloWorldServlet extends HttpServlet {
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print("<html>");
        out.print("<body>");
        out.print("<p>The time is "+ new Date() +"</p>");
        out.print("</body>");
        out.println("</html");
    }
}
