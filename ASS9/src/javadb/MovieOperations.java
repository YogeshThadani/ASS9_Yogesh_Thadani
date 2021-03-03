package javadb;

import java.util.*;
import java.io.*;
import java.sql.*;

public class MovieOperations {
    static  List<Movie> movies;
    
    static
    {
        movies = new ArrayList<>();
    }

    static List<Movie> populateMovies(File f) throws IOException
    {
        FileInputStream fin = new FileInputStream(f);
        Scanner scan = new Scanner(fin);
        String data[];
        Movie m;
        String t, temp;
        List<String> cast = new ArrayList<>();
        int i;
        
        while(scan.hasNextLine())
        {
            cast = new ArrayList<>();
            data = scan.nextLine().split(",");
            m = new Movie();
            
            //intialize movie object
            m.setMovieId(Integer.parseInt(data[0].trim()));
            m.setMovieName(data[1].trim());
            m.setLanguage(data[2].trim());
            m.setReleaseDate(java.sql.Date.valueOf(data[3].trim()));
            m.setMovieType(data[4].trim());
            t = data[5];
            temp="";
            for(i=0; i<t.length(); i++)
            {
                if(t.charAt(i) == '|')
                {
                    cast.add(temp);
                    temp ="";
                }
                else
                    temp+=t.charAt(i);
            }
             cast.add(temp);
            m.setCasting(cast);
            m.setRating(Double.parseDouble(data[6].trim()));
            m.setTotalBusinessDone(Double.parseDouble(data[7].trim()));
            
            //add the object to list
            movies.add(m);
        }
        
        scan.close();
        fin.close();
        
        return movies;
    }//loadDataFromFile
    
    static Connection getConnection() throws Exception
    {
        String url, uname, pwd;
        
        url="jdbc:mysql://localhost:3306/practice";
        uname = "root";
        pwd = "";
        
        Class.forName("com.mysql.cj.jdbc.Driver");
    
        return DriverManager.getConnection(url, uname, pwd);
    }
    
    
    static boolean allAllMoviesInDb(List<Movie> movies) throws Exception
    {
        Connection conn;
        String url, uname, pwd, sql, mname;
        PreparedStatement ps;
        int i;
        Movie m;
        
        conn = getConnection();
        
        //insert into movies tables
        sql = "insert into Movies(mname, mlang, mdate, mtype, mrating, mcollection) values(?,?,?,?,?,?)";
        ps =conn.prepareStatement(sql);
        
        for(i=0; i<movies.size(); i++)
        {
            try
            {
                m = movies.get(i);
                ps.setString(1, m.getMovieName());
                ps.setString(2, m.getLanguage());
                ps.setDate(3, m.getReleaseDate());
                ps.setString(4, m.getMovieType());
                ps.setDouble(5, m.getRating());
                ps.setDouble(6, m.getTotalBusinessDone());
                ps.execute();
            }
            catch(Exception ex){}
        }
        ps.close();
        List<String> cast;
        
        //insert movie cast details
        sql = "insert into castname(name) values(?)";
        ps = conn.prepareStatement(sql);
        
        for(Movie x: movies)
        {
            cast = x.getCasting();
            
            for(i=0; i<cast.size(); i++)
            {
                try
                {
                    ps.setString(1, cast.get(i));
                    ps.execute();
                }
                catch(Exception ex){}
            }//for(i
        }//for
        
        ps.close();
        
        //join the two tables
        sql = "insert into mcast(mid, mcid) values(?, ?)";
        ps = conn.prepareStatement(sql);
        Statement stmt = conn.createStatement();
        ResultSet rs;
        int id=0;
        String n;
        
        for(Movie x: movies)
        {
            cast = x.getCasting();
            mname = x.getMovieName();
            rs = stmt.executeQuery("select mid from movies where mname like '%"+mname+"%' ");
            if(rs.next())
                id = rs.getInt(1);
            ps.setInt(1, id);
            
            rs.close();
            
            for(i=0; i<cast.size(); i++)
            {
                n = cast.get(i);
                rs = stmt.executeQuery("select mcid from castname where name like '%"+n+"%' ");
                if(rs.next())
                    id = rs.getInt(1);
                ps.setInt(2, id);
                ps.execute();
                rs.close();
            }
        }
        
        
        System.out.println("Insertion Success");
        conn.close();
        stmt.close();
        ps.close();
        return true;
    }
    
    static void addMovie(Movie m, List<Movie> movies) throws Exception
    {
        Scanner scan = new Scanner(System.in);
        Language l = new Language();
        Category c = new Category();
        int i, n;
        List<String> cast = new ArrayList<>();
        List<Movie> t = new ArrayList<>();
        
        System.out.println("Enter movie id : ");
        m.setMovieId(scan.nextInt());
        System.out.println("Enter movie name : ");
        scan.nextLine();
        m.setMovieName(scan.nextLine());
        System.out.println("Enter movie language : ");
        m.setLanguage(scan.nextLine());
        System.out.println("Enter release date (in YYYY-MM-DD) : ");
        m.setReleaseDate(java.sql.Date.valueOf(scan.nextLine()));
        System.out.println("Enter Movie Category : ");
        m.setMovieType(scan.nextLine());
        System.out.println("How many actors/actree played the role in movie ?: ");
        n = scan.nextInt();
        scan.nextLine();
        System.out.println("Enter the name of the " + n + " actors/actress : ");
        for(i=0; i<n; i++)
            cast.add(scan.nextLine());
        
        m.setCasting(cast);
        System.out.println("Enter the imdb rating : ");
        m.setRating(scan.nextDouble());
        System.out.println("Enter total business done : ");
        m.setTotalBusinessDone(scan.nextDouble());
        
        t.add(m);
        
        //add to the database
        allAllMoviesInDb(t);
        
        //add to list
        movies.add(m);
    }//addMovie
    
    static List<Movie> getMoviesRealeasedInYear(int year)
    {
        List<Movie> m = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        java.sql.Date d;
        int y;
        
        for(Movie x : movies)
        {
            d = x.getReleaseDate();
            c.setTime(d);
            y =c.get(Calendar.YEAR);
            if(y == year)
                m.add(x);
        }
        
        return m;
    }//getMoviesRealeasedInYear
    
    static List<Movie> getMovieByActor(String ...actorNames)
    {
        List<Movie> m =new ArrayList<>();
        
        for(Movie x : movies)
        {
            List<String> t = x.getCasting();
            if(t.contains(actorNames[0]) || t.contains(actorNames[1]))
                m.add(x);
        }
        
        return m;
    }
    
    static void updateRatings(String s, double rating) throws Exception
    {
        for(Movie x : movies)
        {
            if(x.getMovieName().equals(s))
            {
                x.setRating(rating);
                break;
            }
        }//for
        
        //update database
        Connection conn = getConnection();
        
        Statement stmt = conn.createStatement();
        
        stmt.executeUpdate("update movies set mrating = '"+rating+"' where mname like '%"+s+"%' ");
        
        
        System.out.println("Updated Successfully!!");
        stmt.close();
        conn.close();
    }//updateRatings
    
    static void updateBusiness(String s, double amt) throws Exception
    {
        for(Movie x : movies)
        {
            if(x.getMovieName().equals(s))
            {
                x.setTotalBusinessDone(amt);
                break;
            }
        }//for
        
        //update database
        Connection conn = getConnection();
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("update movies set mcollection = '"+amt+"' where mname like '%"+s+"%' ");
        
        
        System.out.println("Updated Successfully!!");
        stmt.close();
        conn.close();
    }
    
    static List<Movie> deserialize(String fname) 
    {
        List<Movie> temp = new ArrayList<Movie>();
        try
        {
            FileInputStream file = new FileInputStream(new File(fname)); 
            ObjectInputStream in = new ObjectInputStream(file); 
            
            // Method for deserialization of object 
            for(int i=0; i<movies.size(); i++)
                temp.add((Movie)in.readObject());
            
            in.close(); 
            file.close(); 
              
        }
        catch (IOException ex) { 
            System.out.println(ex.getMessage()); 
        } 
  
        catch (ClassNotFoundException ex) { 
            System.out.println("ClassNotFoundException" + 
                                " is caught"); 
        } 
        
            return temp;
    }
    
    static void serialize(String fname) 
    {
        try
        {
            FileOutputStream file = new FileOutputStream(new File(fname)); 
            ObjectOutputStream out = new ObjectOutputStream(file); 
              
            // Method for serialization of object 
            for(Movie m : movies)
                out.writeObject(m);
              
            out.close(); 
            file.close(); 
        }
        catch (IOException ex) { 
            System.out.println(ex.getMessage()); 
        } 
  
         
    }
    
    static Set<Movie> buisnessDone(double amt)
    {
        Comparator<Movie> c = new Comparator<Movie>(){
            public int compare(Movie m1, Movie m2)
            {   
                if(m1.getTotalBusinessDone() > m2.getTotalBusinessDone())
                    return -1;
                else if(m1.getTotalBusinessDone() == m2.getTotalBusinessDone())
                    return 0;
                else 
                    return 1;
            }
        };
        Set<Movie> s = new TreeSet<>(c);
        
        for(Movie x : movies)
            if(x.getTotalBusinessDone() > amt)
                s.add(x);
        
        return s;
    }
    
    public static void main(String args[])
    {
        try
        {
            int  ch=0,i, year;
            Movie m;
            List<Movie> temp,l;
            String x;
            Scanner scan = new Scanner(System.in);
            
            //load from file
            movies = populateMovies(new File("d:/Movies.txt"));
            
            //load into db
            allAllMoviesInDb(movies);
            
            while(ch!=9)
            {
                System.out.println("1. Add A new movies");
                System.out.println("2. Search Movies by year : ");
                System.out.println("3. Find movies by actor names : ");
                System.out.println("4. Update Movie Rating");
                System.out.println("5. Update Business done");
                System.out.println("6. Serialize the objects ");
                System.out.println("7. DeSerialize the objects ");
                System.out.println("8. Display movies with more than enetered amount");
                System.out.println("9. Exit ");
                
                System.out.println("Enter choice : ");
                ch  =scan.nextInt();
                
                switch(ch)
                {
                    case 1: addMovie(new Movie(), movies);
                            break;
                            
                    case 2: System.out.println("Enter the Year : ");
                            year = scan.nextInt();
                            l = getMoviesRealeasedInYear(year);
                            if(l.isEmpty())
                                System.out.println("No Movies Found for the entered year "+ year +"!!");
                            else
                            {
                                System.out.println("Movies released in the year "+year+"!!");
                                for(i=0; i<l.size(); i++)
                                    System.out.println(l.get(i).getMovieName());
                            }
                            break;
                            
                    case 3: System.out.println("Enter actor names to search for movie: ");
                            System.out.println("Enter 2 names : ");
                            scan.nextLine();
                            temp = getMovieByActor(scan.nextLine(), scan.nextLine());
                            System.out.println("MOVIES got by searching using entered actors : ");
                            for(i=0; i<temp.size(); i++)
                                System.out.println(temp.get(i).getMovieName());
                            break;
                            
                    case 4: System.out.println("Enter movie Name : ");
                            scan.nextLine();
                            x = scan.nextLine();
                            System.out.println("Enter new rating : ");
                            updateRatings(x, scan.nextDouble());
                            break;
                    
                    case 5: System.out.println("Enter movie name : ");
                            scan.nextLine();
                            x = scan.nextLine();
                            System.out.println("Enter new business collection : ");
                            updateBusiness(x, scan.nextDouble());
                            break;
                          
                    case 6: serialize("e:/file.txt");
                            System.out.println("Serialized!!");
                            break;
                            
                    case 7: temp=deserialize("e:/file.txt");
                            System.out.println("DeSerialized!!");
                            for(Movie w:temp)
                                System.out.println(w.getMovieName());
                            break;
                            
                    case 8: System.out.println("Enter amount : ");
                            double amt =scan.nextDouble();
                            Set<Movie> s = buisnessDone(amt);
                            System.out.println("Movie with business > "+ amt);
                            for(Movie w : s)
                                System.out.println(w.getMovieName());
                            
                            break;
                            
                    case 9: break;
                }//switch
            }//while
            
               
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }
    }
}
