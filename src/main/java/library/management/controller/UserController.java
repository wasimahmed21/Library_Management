package library.management.controller;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import library.management.entities.Admin;
import library.management.entities.Book;
import library.management.entities.BorrowBook;
import library.management.entities.PurchasedBook;
import library.management.entities.User;
import library.management.repositories.AdminLoginDAO;
import library.management.repositories.BookDAO;
import library.management.repositories.UserDAO;

@Controller
@RequestMapping("User")

public class UserController {
	@Autowired
	UserDAO userDAO;
	
	@Autowired
	BookDAO bookDAO;
	
	@Autowired
	AdminLoginDAO adminLoginDAO;
	
	
	@GetMapping("/AdminLogin")
	public String showAdminLoginPage() {
		return "AdminLogin";
	}
	@GetMapping("/UserLogin")
	public String showUserLoginPage() {
		return "UserLogin";
	}
	@GetMapping("/UserRegister")
	public String showUserRegisterForm() {
		return "UserRegister";
	}
	
	@PostMapping("/handle-register")
	public String showUserRegisterPage(
			@RequestParam("emailId") String emailId,
			@RequestParam("userName") String userName,
			@RequestParam("passWord") String passWord
			) {
			int result = userDAO.userRegister(userName, emailId, passWord);
			if(result == 1) {
				return "UserLogin";
			}else {
				return "UserRegister";
			}		
	}
	
	@PostMapping("/validate-admin")
	public String showAdminDashboard(
			@RequestParam("adminEmailId") String adminEmailId,
			@RequestParam("adminPassword") String adminPassword,
			HttpServletRequest request
			) {		
			// Creating admin session
			HttpSession session = request.getSession();
			Admin admin = new Admin();
			// Checking if list is empty,
			// if empty, then no record is fetched,
			// then, admin details is incorrect.
			if(adminLoginDAO.validateAdmin(adminEmailId, adminPassword).isEmpty()) {
				System.out.println("NO user found!");			
				return "AdminLogin";
			}
			// If not null, validated,
			// Dashboard page is shown to admin
			else {
				admin = adminLoginDAO.validateAdmin(adminEmailId, adminPassword).get(0);
				session.setAttribute("adminSession", admin);
				return "AdminDashboard";
			}
	}
	
	@PostMapping("/validate-user")
	public String showUserDashboard(
			@RequestParam("emailId") String userEmailId,
			@RequestParam("passWord") String userPassword,
			HttpServletRequest request
			) {
			HttpSession session = request.getSession();
			User user;
			if(userDAO.userLogin(userEmailId, userPassword).isEmpty()) {
				System.out.println("Invalid Id");
				return "UserLogin";
			}
			else {
				user = userDAO.userLogin(userEmailId, userPassword).get(0);
				session.setAttribute("User", user);
				return "UserDashboard";
			}
	}
	
	@GetMapping("/handle-view-user")
	public String handleViewUser(
			Model model
			) {
			List<User> list = userDAO.viewUser();
			model.addAttribute("userList",list);
			return "ViewUser";
	}
	
	
	@GetMapping("/add-books")
	public String addBooks() {
		return "add-books";
	}
	
	@PostMapping("/handle-add-books")
	public String handleAddBooks(
            @RequestParam("bookName") String bookName,
            @RequestParam("bookPrice") String bookPrice,
            @RequestParam("bookGenre") String bookGenre,
            @RequestParam("bookPublication") String bookPublication,
            @RequestParam("bookPublishDate") String bookPublishDate,
            @RequestParam("bookEdition") String bookEdition,
            @RequestParam("bookQuantity") String bookQuantity,
            @RequestParam("authorName") String authorName,
            @RequestParam("bookCover") MultipartFile bookCover,
            Model model
			) {
		
		
		byte[] bookCoverArr;
		Blob bookCoverBlob = null;
		try {
			bookCoverArr = bookCover.getBytes();
			bookCoverBlob = new SerialBlob(bookCoverArr);
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
		Book book;
		
			book = new Book(bookName, Integer.parseInt(bookPrice), bookGenre,
					bookPublication, Integer.parseInt(bookEdition), Integer.parseInt(bookQuantity),
					Date.valueOf(bookPublishDate), authorName, true, bookCoverBlob);
			int status = userDAO.addBooks(book);
			model.addAttribute("status",status);
				
		return "AdminDashboard";
	}
	
	
	@GetMapping("/place-order")
	public String placeOrder(
			Model model
			) {
		List<Book> books =  bookDAO.viewAllBooks();
		model.addAttribute("books",books);
		return "place-order";
	}
	
	@GetMapping("/handlePlaceOrder")
	public String handlePlaceOrder(@RequestParam("bookId") String bookId, 
			Model model) {
		Book book = bookDAO.displayByBookId(Integer.parseInt(bookId));
		model.addAttribute("book",book);
		return "book-details";
	}
	
	@GetMapping("/confirm-placeorder")
	public String confirmPlaceOrder(
			@RequestParam("bookId") String bookId,
			@RequestParam("count") String bookCount,
			Model model,
			HttpSession session
			) {
		
		System.out.println(bookId + "-" + bookCount);
		User user = (User) session.getAttribute("User");
		 int status = userDAO.placeOrder(user.getUserId(),
				 Integer.parseInt(bookId),Integer.parseInt(bookCount),Date.valueOf(LocalDate.now()));
		 
		 List<PurchasedBook> bookList = userDAO.viewPurchasedBooks(user.getUserId());
		 bookDAO.updateBookCount(Integer.parseInt(bookCount));
		 model.addAttribute("bookList",bookList);
		return "view-your-books";
	}
	
	@GetMapping("/view-your-books")
	public String openOrder(Model model,HttpSession session) {
		User user = (User) session.getAttribute("User");
		 List<PurchasedBook> bookList = userDAO.viewPurchasedBooks(user.getUserId());
		 model.addAttribute("bookList",bookList);

		 return "view-your-books";
	}
	
	
	@GetMapping("/handleBorrowBook")
	public String handleBorrowBooks(@RequestParam("bookId") String bookId, 
			Model model) {
		Book book = bookDAO.displayByBookId(Integer.parseInt(bookId));
		model.addAttribute("book",book);
		return "borrow-book-details";
	}
	
	@GetMapping("/confirm-borrowbook")
	public String confirmBorrowBook(
			@RequestParam("bookId") String bookId,
			@RequestParam("count") String bookCount,
			Model model,
			HttpSession session
			) {
		
		 User user = (User) session.getAttribute("User");
		 int status = bookDAO.updateBorrowBookCount(user.getUserId(),
				 			Integer.parseInt(bookId), Date.valueOf(LocalDate.now()),
				 			Date.valueOf(LocalDate.now().plusWeeks(2)));
		 
		List<BorrowBook> borrowBooks = userDAO.viewBorrowedBooks(user.getUserId());
		
		model.addAttribute("borrowedBooks", borrowBooks);
		
		return "view-borrowed-books";
	}
	
	//open delete-user.jsp file
	@GetMapping("/delete-user")
	public String openDeleteUser() {
		return "delete-user";
	}
	
	@GetMapping("/deleteuser")
	public String deleteuser(
			@RequestParam("userid") String userId,
			Model model,
			HttpSession session
			) {

		User status=userDAO.checkUser(Integer.parseInt(userId));
		System.out.println(status.getUserId());
		if(status!=null) {//delete user
			
			int deleteStatus=userDAO.deleteUser(Integer.parseInt(userId));// 0 -> return (""
			if(deleteStatus==0) {
				model.addAttribute("message","This user has fine or not returned the book!");
			}
			else {
			model.addAttribute("message","Deleted successfully!");
		}
		}else {
			model.addAttribute("message","No user found!");
		}
		

		
		return "delete-user";
	}
	
	@GetMapping("/delete-books")
	public String openDeletebook() {
		return "delete-book";
	}
	
	@GetMapping("/deletebook")
	public String deletebook(
			@RequestParam("bookid") String bookId,
			Model model,
			HttpSession session
			) {

		Book status=userDAO.checkBook(Integer.parseInt(bookId));
		
		if(status!=null) {//delete user
			
			int deleteStatus=userDAO.deleteBook(Integer.parseInt(bookId));// 0 -> return (""
			if(deleteStatus==0) {
				model.addAttribute("message","This book has already borrowed by some user!");
			}
			else {
			model.addAttribute("message","Book Deleted successfully!");
		}
		}else {
			model.addAttribute("message","No Book found!");
		}
		
		return "delete-book";
	}
	@GetMapping("/view-particular-user")
	public String openviewparticularuser() {
		return "view-particular-user";
	}
	
	@GetMapping("/viewParticularUser")
	public String viewparticularuser(@RequestParam("userid") String userId,
			Model model,
			HttpSession session) {
		User userDetails=userDAO.checkUser(Integer.parseInt(userId));
		model.addAttribute("userInfo",userDetails);
		return "view-particular-user";
	}
}
