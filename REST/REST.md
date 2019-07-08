# RESTful service with CFX

![](Untitled-12dccec4-40ac-455c-b12d-ea19cb11611c.png)

## Guide

### Server

    @XmlRootElement(name = "Course")
    public class Course {
    	
    		@GET
        @Path("{studentId}")
        public Student getStudent(@PathParam("studentId")int studentId) {
            return findById(studentId);
        }
    
    		@POST
        @Path("")
        public Response createStudent(Student student) {
            for (Student element : students) {
                if (element.getId() == student.getId()) {
                    return Response.status(Response.Status.CONFLICT).build();
                }
            }
            students.add(student);
            return Response.ok(student).build();
        }
        
        @DELETE
        @Path("{studentId}")
        public Response deleteStudent(@PathParam("studentId") int studentId) {
            Student student = findById(studentId);
            if (student == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            students.remove(student);
            return Response.ok().build();
        }
    
    }

### Client

Creare classi di cui si ha bisogno (esempio Course e Student)

    WebClient client = WebClient.create("http://localhost:8080/course");
    
    //GET course
    Course course = client.path("courses/1").accept("text/xml").get().readEntity(Course.class);
    System.out.println(course.getName());
    
    //POST student
    Student student = new Student();
    student.setId(100);
    student.setName("MASSIMO DECIMO MECELLO");
    Response r = client.path("students").post(student);
    System.out.println(r.getStatus());

## Server

### Main

    public class MyServer {
        public static void main(String[] args) throws Exception{
            JAXRSServerFactoryBean factoryBean = new JAXRSServerFactoryBean();
            factoryBean.setResourceClasses(CourseRepository.class);
            factoryBean.setResourceProvider(new SingletonResourceProvider(new CourseRepository()));
            factoryBean.setAddress("http://localhost:8080/");	
            Server server = factoryBean.create();
        }
    }

### CourseRepository

    @Path("course")
    @Produces("text/xml")
    public class CourseRepository {
        private Map<Integer, Course> courses = new HashMap<>();
        
        public CourseRepository(){
            Student student1 = new Student();
            Student student2 = new Student();
            student1.setId(1);
            student1.setName("Student A");
            student2.setId(2);
            student2.setName("Student B");
    
            List<Student> course1Students = new ArrayList<>();
            course1Students.add(student1);
            course1Students.add(student2);
    
            Course course1 = new Course();
            Course course2 = new Course();
            course1.setId(1);
            course1.setName("REST with Spring");
            course1.setStudents(course1Students);
            course2.setId(2);
            course2.setName("Learn Spring Security");
    
            courses.put(1, course1);
            courses.put(2, course2);
        }
    
        @GET
        @Path("courses/{courseId}")
        public Course getCourse(@PathParam("courseId") int courseId) {
            return findById(courseId);
        }
        
        @PUT
        @Path("courses/{courseId}")
        public Response updateCourse(@PathParam("courseId") int courseId, Course course) {
            Course existingCourse = findById(courseId);        
            if (existingCourse == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (existingCourse.equals(course)) {
                return Response.notModified().build();    
            }
            courses.put(courseId, course);
            return Response.ok().build();
        }
        
        @Path("courses/{courseId}/students")
        public Course pathToStudent(@PathParam("courseId") int courseId) {
            return findById(courseId);
        }
     
        private Course findById(int id) {
            for (Map.Entry<Integer, Course> course : courses.entrySet()) {
                if (course.getKey() == id) {
                    return course.getValue();
                }
            }
            return null;
        }
    }

### Student

    @XmlRootElement(name = "Student")
    public class Student {
        private int id;
        private String name;
    
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + this.id;
            hash = 89 * hash + Objects.hashCode(this.name);
            return hash;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Student other = (Student) obj;
            if (this.id != other.id) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return true;
        }
        
    }

### Course

    @XmlRootElement(name = "Course")
    public class Course {
        private int id;
        private String name;
        private List<Student> students = new ArrayList<>();
        
        @GET
        @Path("{studentId}")
        public Student getStudent(@PathParam("studentId")int studentId) {
            return findById(studentId);
        }
        
        @POST
        @Path("")
        public Response createStudent(Student student) {
            for (Student element : students) {
                if (element.getId() == student.getId()) {
                    return Response.status(Response.Status.CONFLICT).build();
                }
            }
            students.add(student);
            return Response.ok(student).build();
        }
        
        @DELETE
        @Path("{studentId}")
        public Response deleteStudent(@PathParam("studentId") int studentId) {
            Student student = findById(studentId);
            if (student == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            students.remove(student);
            return Response.ok().build();
        }
     
        private Student findById(int id) {
            for (Student student : students) {
                if (student.getId() == id) {
                    return student;
                }
            }
            return null;
        }
    
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public List<Student> getStudents() {
            return students;
        }
    
        public void setStudents(List<Student> students) {
            this.students = students;
        }
    
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + this.id;
            hash = 97 * hash + Objects.hashCode(this.name);
            hash = 97 * hash + Objects.hashCode(this.students);
            return hash;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Course other = (Course) obj;
            if (this.id != other.id) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.students, other.students)) {
                return false;
            }
            return true;
        }
        
    }

## Client

### Main

    public class MyClient {
        public static void main(String[] args) throws Exception{
            WebClient client = WebClient.create("http://localhost:8080/course");
            
            //GET course
            Course course = client.path("courses/1").accept("text/xml").get().readEntity(Course.class);
            System.out.println(course.getName());
            
            //POST student
            Student student = new Student();
            student.setId(100);
            student.setName("MASSIMO DECIMO MECELLO");
            Response r = client.path("students").post(student);
            System.out.println(r.getStatus());
            
            //GET student
            Student mecello = client.path("100").get().readEntity(Student.class);
            System.out.println(mecello.getName());
            
        }
    }

### Student

    @XmlRootElement(name = "Student")
    public class Student {
        private int id;
        private String name;
    
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + this.id;
            hash = 89 * hash + Objects.hashCode(this.name);
            return hash;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Student other = (Student) obj;
            if (this.id != other.id) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return true;
        }   
    }

### Course

    @XmlRootElement(name = "Course")
    public class Course {
        private int id;
        private String name;
        private List<Student> students = new ArrayList<>();
    
        private Student findById(int id) {
            for (Student student : students) {
                if (student.getId() == id) {
                    return student;
                }
            }
            return null;
        }
    
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public List<Student> getStudents() {
            return students;
        }
    
        public void setStudents(List<Student> students) {
            this.students = students;
        }
    
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + this.id;
            hash = 97 * hash + Objects.hashCode(this.name);
            hash = 97 * hash + Objects.hashCode(this.students);
            return hash;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Course other = (Course) obj;
            if (this.id != other.id) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.students, other.students)) {
                return false;
            }
            return true;
        }
        
    }