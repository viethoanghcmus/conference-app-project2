package DAO;

import DTO.UserDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import pojo.Attends;
import pojo.Conference;
import pojo.User;
import utils.HibernateUtils;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static int getMaxIUser(){
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.openSession();
        try{
            session.getTransaction().begin();
            String hql = "select max(u.id) from User u";
            Query<Integer> query = session.createQuery(hql);
            session.getTransaction().commit();
            return query.list().get(0);

        }catch (Exception e ){
            e.printStackTrace();
            session.getTransaction().rollback();
            return -1;
        }
    }

    public static void insertUser(User user){
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.openSession();
        try{
            session.getTransaction().begin();
                if(user.getId()!=0){
                    session.save(user);
                }
            session.getTransaction().commit();
        }catch (Exception e ){
            e.printStackTrace();
            session.getTransaction().rollback();
        }
    }

    public static boolean checkExistsUsernameAndEmail(User user){
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.openSession();
        try{
            session.getTransaction().begin();
            String hql = "select u.id from User u where u.username = '" +user.getUsername()
                    + "' or u.email = '"+user.getEmail()+"'";
            Query<Integer> query = session.createQuery(hql);
            if (query.list().size()==0) return false;
            return true;
        }catch (Exception e ){
            e.printStackTrace();
            session.getTransaction().rollback();
            return false;
        }
    }

    public static User getUser(String username, String password){
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.openSession();
        List<User> list = new ArrayList<>();
        try{
            session.getTransaction().begin();
            String hql = "select u from User u where u.username = '" +username + "' and u.password = '" + password+"'" ;
            Query<User> query = session.createQuery(hql);
            list = query.list();
            session.getTransaction().commit();
            if (list.size() == 0) return null;
            else return list.get(0);

        }catch (Exception e ){
            e.printStackTrace();
            session.getTransaction().rollback();
            return null;
        }
    }

    public static ObservableList<UserDTO> getUserList() {
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.openSession();
        List<User> list = new ArrayList<>();
        List<UserDTO> listDTO = new ArrayList<>();
        try{
            session.getTransaction().begin();
            String hql = "from User" ;
            Query query = session.createQuery(hql);
            list = query.list();
            for (User u:list){
                listDTO.add(u.getUserDetail());
            }
            return FXCollections.observableList(listDTO);
        }catch (Exception e ){
            e.printStackTrace();
            session.getTransaction().rollback();
            return null;
        }

    }

    public static void updateUser(int id,boolean active){
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.openSession();
        try{
            session.getTransaction().begin();
            String hql = "update User u set u.active = :active where u.id= :id" ;
            Query query = session.createQuery(hql);
            query.setParameter("active",active);
            query.setParameter("id",id);
            query.executeUpdate();
            session.getTransaction().commit();
        }catch (Exception e ){
            e.printStackTrace();
            session.getTransaction().rollback();
        }
    }
}