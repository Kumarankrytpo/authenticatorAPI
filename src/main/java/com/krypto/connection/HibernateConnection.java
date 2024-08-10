package com.krypto.connection;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateConnection {
    private static SessionFactory sessionfactory;
    public HibernateConnection(){
      try{
          sessionfactory = new Configuration().configure().addPackage("com.krypto.Entities").buildSessionFactory();
      }catch(Exception e){
          e.printStackTrace();
      }
    }

    public static SessionFactory getSessionfactory() {
        return sessionfactory;
    }

    public static void setSessionfactory(SessionFactory sessionfactory) {
        HibernateConnection.sessionfactory = sessionfactory;
    }

    public void shutDownFactory(){
        if(sessionfactory!=null){
            sessionfactory.close();
        }
    }
}
