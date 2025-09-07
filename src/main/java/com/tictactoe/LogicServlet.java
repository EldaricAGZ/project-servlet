package com.tictactoe;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) {
        HttpSession currentSession = req.getSession();
        Field field = extractField(currentSession);

        int index = getSelectedIndex(req);

        if (field.getField().get(index) != Sign.EMPTY) {
            try {
                getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
            } catch (IOException | ServletException e) {
                e.printStackTrace();
            }
            return;
        }
        field.getField().put(index, Sign.CROSS);

        if (checkWin(currentSession, resp, field)) {
            return;
        }

        int emptyFieldIndex = field.getEmptyFieldIndex();

        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(currentSession, resp, field)) {
                return;
            }
        } else {
            currentSession.setAttribute("draw", true);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);
            try {
                resp.sendRedirect("index.jsp");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        List<Sign> data = field.getFieldData();

        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        try {
            resp.sendRedirect("/index.jsp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getSelectedIndex(HttpServletRequest request) {
        Integer click = Integer.parseInt(request.getParameter("click"));
        boolean isNumeric = click.toString().chars().allMatch(Character::isDigit);
        return isNumeric ? click : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    private boolean checkWin(HttpSession session, jakarta.servlet.http.HttpServletResponse resp, Field field) {
        Sign sign = field.checkWin();
        if (sign == Sign.NOUGHT || sign == Sign.CROSS) {
            session.setAttribute("winner", sign);

            List<Sign> data = field.getFieldData();

            session.setAttribute("field", field);
            session.setAttribute("data", data);

            try {
                resp.sendRedirect("index.jsp");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
