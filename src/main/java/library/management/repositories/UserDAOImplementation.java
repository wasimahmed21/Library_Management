package library.management.repositories;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import library.management.entities.Book;
import library.management.entities.BorrowBook;
import library.management.entities.PurchasedBook;
import library.management.entities.User;

public class UserDAOImplementation implements UserDAO{

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public int userRegister(String userName, String userEmailId, String userPassword) {
		String userRegisterQuery = "INSERT INTO user (userName, userEmailId, userPassword) VALUES (?,?,?)";
		return jdbcTemplate.update(userRegisterQuery,userName,userEmailId,userPassword);
	}

	@Override
	public List<User> userLogin(String userEmailId, String userPassword) {
		String userLoginQuery = "SELECT * FROM user WHERE userEmailId = ? and userPassword = ?";
		return jdbcTemplate.query(userLoginQuery, new UserLoginRowMapper(),userEmailId,userPassword);
	}

	@Override
	public List<User> viewUser() {
		String viewUserQuery = "SELECT * FROM user";
		return jdbcTemplate.query(viewUserQuery, new ViewUserRowMapper());
	}

	@Override
	public int addBooks(Book book) {

		 String insertQuery = "INSERT INTO books " +
	                "(bookName, bookPrice, bookGenre, bookPublication, " +
	                "bookPublishDate, bookEdition, bookQuantity, authorName, bookCover, bookStatus) " +
	                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		 
		 return jdbcTemplate.update(insertQuery,
				 				book.getBookName(),
				 				book.getBookPrice(),
				 				book.getBookGenre(),
				 				book.getBookPublication(),
				 				book.getPublishDate(),
				 				book.getBookEdition(),
				 				book.getBookQuantity(),
				 				book.getAuthorName(),
				 				book.getBookCover(),
				 				book.isBookStatus());
		
	}

	@Override
	public int placeOrder(int userId,int bookId, int bookCount,Date orderDate) {
		String placeOrderQuery = "insert into orderedbooks values (?,?,?,?)";
		return jdbcTemplate.update(placeOrderQuery,userId,bookId,bookCount,orderDate);
	}

	@Override
	public List<PurchasedBook> viewPurchasedBooks(int userId) {
		String viewPurchasedBookQuery = "select books.bookName,books.bookPrice, books.bookCover, "
				+ "orderedbooks.orderDate,orderedbooks.bookCount from books "
				+ " inner join orderedbooks \r\n"
				+ "on books.bookId = orderedbooks.bookId\r\n"
				+ "where orderedbooks.userId = ?;";
			return jdbcTemplate.query(viewPurchasedBookQuery, new OrderedBookRowMapper(), userId);
	}

	@Override
	public List<BorrowBook> viewBorrowedBooks(int userId) {
		String viewBorrowedBooksQuery = " select books.bookCover,\r\n"
				+ "books.bookName,\r\n"
				+ "borrowbook.borrowedDate,\r\n"
				+ "borrowbook.returnDate,\r\n"
				+ "borrowbook.bookfine,\r\n"
				+ "borrowbook.borrowedId from books inner join borrowbook\r\n"
				+ "on \r\n"
				+ "books.bookId = borrowbook.bookId\r\n"
				+ "where\r\n"
				+ "borrowbook.userId = ? ";
		
		return jdbcTemplate.query(viewBorrowedBooksQuery, new BorrowRowMapper(), userId);
	}
	
	@Override
	public User checkUser(int userId) {
		String checkAvailableUser = "select * from user where userId = ? and status = 1";
		
		return jdbcTemplate.queryForObject(checkAvailableUser, new UserLoginRowMapper(),userId);
		
		
	}
	@Override
	public int deleteUser(int userId) {
		String deleteQuery = "UPDATE user\n"
				+ "SET status = 0\n"
				+ "WHERE userId = ? -- specify the user ID\n"
				+ "  AND userId NOT IN (SELECT userId FROM borrowbook WHERE bookFine > 0)\n"
				+ "  AND userId NOT IN (SELECT userId FROM borrowbook WHERE returnStatus = 0)";
		return jdbcTemplate.update(deleteQuery, userId);
		
		
	}
	@Override
	public Book checkBook(int bookId) {
		String checkAvailableBook = "select * from books where bookId = ? and bookStatus = 1";
		
		return jdbcTemplate.queryForObject(checkAvailableBook, new BookRowMapper(),bookId);
		
		
	}
	@Override
	public int deleteBook(int bookId) {
		String deleteQuery = "UPDATE books\n"
				+ "SET bookStatus = 0\n"
				+ "WHERE bookId = ? -- specify the user ID\n"
				+ "  AND bookId NOT IN (SELECT bookId FROM borrowbook)";
		return jdbcTemplate.update(deleteQuery, bookId);
		
		
	}
	

}
