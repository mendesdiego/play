import java.util.List;

import models.Post;
import models.Tag;
import models.User;

import org.h2.engine.Comment;
import org.hibernate.mapping.Map;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class BasicTest extends UnitTest {
	
	@Before
	public void setup(){
		Fixtures.deleteDatabase();
	}
	
	@Test
	public void createAndRetrieveUser(){
		// Criando um Usuario
		new User("bob@email.com","password","Bob Silva").save();
		
		// Recuperando o usuario criado
		User bob = User.find("byEmail", "bob@email.com").first();
		
		// Testes
		assertNotNull(bob);
		assertEquals("Bob Silva", bob.fullname);
	}
	
	@Test
	public void tryConnectAsUser() {
		//Cria um usuario e o salva
		new User("bob@gmail.com","secret","Bob").save();
		
		//Teste
		assertNotNull(User.connect("bob@gmail.com", "secret"));
		//assertNotNull(User.connect("bob@gmail.com", "badpassword"));
		//assertNotNull(User.connect("tom@gmail.com", "secret"));
	}
	
	@Test
	public void createPost(){
		// Cria um novo usuario e o salva
		User bob = new User("bob@email.com","secret", "Bob").save();
		
		// Cria um novo Post
		new Post(bob, "My First Post", "Hello World!").save();
		
		// Testa se o post foi criado com sucesso
		assertEquals(1,Post.count());
		
		// Recupera todos os posts feitos por Bob
		List<Post> bobPosts = Post.find("byAuthor", bob).fetch();
		
		// Testes
		assertEquals(1,bobPosts.size());
		Post firstPost = bobPosts.get(0);
		assertNotNull(firstPost);
		assertEquals(bob, firstPost.author);
		assertEquals("My First Post", firstPost.title);
		assertEquals("Hello World!", firstPost.content);
		assertNotNull(firstPost.postedAt);
	}
	
	@Test
	public void postComments(){
		// Cria um novo usuario e o salva
		User bob = new User("bob@email.com","secret", "Bob").save();
		
		// Cria um novo Post
		Post bobPost = new Post(bob, "My First Post", "Hello World!").save();
		
		// Postando um primeiro comentario
		new models.Comment(bobPost, "Jeff", "Nice post").save();
		new models.Comment(bobPost, "Tom", "I knew that !").save();
		
		// Recuperando todos os comentarios
		List<models.Comment> bobPostsComments = models.Comment.find("byPost", bobPost).fetch();
		
		// Testes
		
		assertEquals(2,bobPostsComments.size());
		
		models.Comment firstComment = bobPostsComments.get(0);
		assertNotNull(firstComment);
		assertEquals("Jeff",firstComment.author);
		assertEquals("Nice post",firstComment.content);
		assertNotNull(firstComment.postedAt);
		
		models.Comment secondComment = bobPostsComments.get(1);
		assertNotNull(secondComment);
		assertEquals("Tom",secondComment.author);
		assertEquals("I knew that !",secondComment.content);
		assertNotNull(secondComment.postedAt);
		
		
	}
	
	@Test
	public void useTheCommentsRelation() {
	    // Create a new user and save it
	    User bob = new User("bob@gmail.com", "secret", "Bob").save();
	 
	    // Create a new post
	    Post bobPost = new Post(bob, "My first post", "Hello world").save();
	 
	    // Post a first comment
	    bobPost.addComment("Jeff", "Nice post");
	    bobPost.addComment("Tom", "I knew that !");
	 
	    // Count things
	    assertEquals(1, User.count());
	    assertEquals(1, Post.count());
	    assertEquals(2, models.Comment.count());
	 
	    // Retrieve Bob's post
	    bobPost = Post.find("byAuthor", bob).first();
	    assertNotNull(bobPost);
	 
	    // Navigate to comments
	    assertEquals(2, bobPost.comments.size());
	    assertEquals("Jeff", bobPost.comments.get(0).author);
	    
	    // Delete the post
	    bobPost.delete();
	    
	    // Check that all comments have been deleted
	    assertEquals(1, User.count());
	    assertEquals(0, Post.count());
	    assertEquals(0, models.Comment.count());
	}
	
	@Test
	public void fullTest() {
	    Fixtures.loadModels("data.yml");
	 
	    // Count things
	    assertEquals(2, User.count());
	    assertEquals(3, Post.count());
	    assertEquals(3, models.Comment.count());
	 
	    // Try to connect as users
	    assertNotNull(User.connect("bob@gmail.com", "secret"));
	    assertNotNull(User.connect("jeff@gmail.com", "secret"));
	    assertNull(User.connect("jeff@gmail.com", "badpassword"));
	    assertNull(User.connect("tom@gmail.com", "secret"));
	 
	    // Find all of Bob's posts
	    List<Post> bobPosts = Post.find("author.email", "bob@gmail.com").fetch();
	    assertEquals(2, bobPosts.size());
	 
	    // Find all comments related to Bob's posts
	    List<Comment> bobComments = models.Comment.find("post.author.email", "bob@gmail.com").fetch();
	    assertEquals(3, bobComments.size());
	 
	    // Find the most recent post
	    Post frontPost = Post.find("order by postedAt desc").first();
	    assertNotNull(frontPost);
	    assertEquals("About the model layer", frontPost.title);
	 
	    // Check that this post has two comments
	    assertEquals(2, frontPost.comments.size());
	 
	    // Post a new comment
	    frontPost.addComment("Jim", "Hello guys");
	    assertEquals(3, frontPost.comments.size());
	    assertEquals(4, models.Comment.count());
	}
	
	@Test
	public void testTags() {
	    // Create a new user and save it
	    User bob = new User("bob@gmail.com", "secret", "Bob").save();
	 
	    // Create a new post
	    Post bobPost = new Post(bob, "My first post", "Hello world").save();
	    Post anotherBobPost = new Post(bob, "Hop", "Hello world").save();
	 
	    // Well
	    assertEquals(0, Post.findTaggedWith("Red").size());
	 
	    // Tag it now
	    bobPost.tagItWith("Red").tagItWith("Blue").save();
	    anotherBobPost.tagItWith("Red").tagItWith("Green").save();
	 
	    // Check
	    assertEquals(2, Post.findTaggedWith("Red").size());
	    assertEquals(1, Post.findTaggedWith("Blue").size());
	    assertEquals(1, Post.findTaggedWith("Green").size());
	    assertEquals(1, Post.findTaggedWith("Red", "Blue").size());
	    assertEquals(1, Post.findTaggedWith("Red", "Green").size());
	    assertEquals(0, Post.findTaggedWith("Red", "Green", "Blue").size());
	    assertEquals(0, Post.findTaggedWith("Green", "Blue").size());
	    
	    List<Map> cloud = Tag.getCloud();
	    assertEquals(
	        "[{tag=Blue, pound=1}, {tag=Green, pound=1}, {tag=Red, pound=2}]",
	        cloud.toString()
	    );
	}
	
	

    
}
