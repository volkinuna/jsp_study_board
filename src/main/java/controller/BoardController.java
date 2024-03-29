package controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.List;

import javax.security.auth.message.callback.PrivateKeyCallback.Request;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.beanutils.BeanUtils;

import DAO.BoardDAO;
import DTO.Board;
import oracle.net.aso.e;

/**
 * Servlet implementation class BoardController
 */
// 웹 어플리케이션에서 발생하는 모든 request는 전부 BoardController로 온다.
@WebServlet("/")
@MultipartConfig(maxFileSize=1024*1024*2, location="c:/Temp/img")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private BoardDAO dao;
    private ServletContext ctx;
    
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		// init은 서블릿 객체 생성시 딱 한번만 실행되므로 객체를 한번만 생성해 공유한다.
		dao = new BoardDAO();
		ctx = getServletContext(); // ServletContext : 웹 어플리케이션의 자원관리(메모리, 어떻게 실행되는지 등)
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8"); // request 객체 한글 깨짐 방지
		
		String command = request.getServletPath();
		String site = null;
		
		System.out.println("command : " + command);
		
		switch (command) {
		case "/index": site = getList(request); break; // index.jsp로 이동
		case "/view": site = getview(request); break;  // view.jsp로 이동
		case "/write": site = "write.jsp"; break;      // write.jsp로 이동
		case "/insert": site = insertBoard(request); break;  // 게시글 등록
		case "/edit": site = getViewForEdit(request); break; // edit.jsp로 이동
		case "/update": site = updateBoard(request); break;  // 게시글 수정
		case "/delete": site = deleteBoard(request); break;  // 게시글 삭제
		}
		
		/*
		 - 공통점 : 둘다 페이지를 이동한다.
		 - 차이점 ┐
		  redirect : 객체(request, response)를 가지고 이동하지 않는다. URL의 변화
		          * DB에 변화가 생기는 요청에 사용(insert, update, delete) - 글쓰기, 글수정, 글삭제, 회원가입 등
		  forward : 객체(request, response)를 가지고 이동한다. URL의 변화가 없다.
		         * 단순 조회에 사용(select) - 게시글 목록보기, 게시글 상세페이지, 검색 등
		*/
		
		// redirect:/index
		if (site.startsWith("redirect:/")) { // redirect 처리
			String rview = site.substring("redirect:/".length()); // index번호 : 10
			response.sendRedirect(rview); // rview = /index
		} else { // forward 처리
			ctx.getRequestDispatcher("/" + site).forward(request, response);
		}
	}

	// BoardDAO 객체의 getList 메소드 실행 : 게시물 전체 목록을 가져온 후 request 객체에 넣어준다.
	public String getList(HttpServletRequest request) {
		List<Board> list;
		
		try {
			list = dao.getList();
			request.setAttribute("boardList", list); // 게시물 전체 목록이 들어있는 list를 boardList에 담아 보낸다.
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "게시물 목록을 정상적으로 가져오지 못했습니다.");
		}
		
		return "index.jsp";
	}
	
	// 게시물의 상세페이지를 가지고와서 request 객체에 넣어준다.
	public String getview(HttpServletRequest request) {
		// 쿼리 파라메터에 있는 board_no값을 가져온다.
		int board_no = Integer.parseInt(request.getParameter("board_no"));
		
		try {
			dao.updateViews(board_no); // 조회수 증가
			Board b = dao.getView(board_no);
			request.setAttribute("board", b);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "게시글을 정상적으로 가져오지 못했습니다.");
		}
		
		return "view.jsp";
	}
	
	// 수정할 게시물의 기존 데이터를 가지고 와서 request 객체에 넣어준다.
	public String getViewForEdit(HttpServletRequest request) {
		// 쿼리 파라메터에 있는 board_no값을 가져온다.
		int board_no = Integer.parseInt(request.getParameter("board_no"));
				
		try {
			Board b = dao.getView(board_no);
			request.setAttribute("board", b);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return "edit.jsp";
	}
	
	// 게시물을 등록해준다.
	public String insertBoard(HttpServletRequest request) {
		Board b = new Board(); 
		
		try {
			BeanUtils.populate(b, request.getParameterMap());
			
			// 1. 이미지 파일을 서버(c:/Temp/img) 컴퓨터에 저장
			Part part = request.getPart("file"); // Part : 파일을 받기위한 클래스 타입
			String fileName = getFilename(part); // 파일에 대한 정보를 가지고 있는 part를 매개변수로 받는다.(파일명 얻음)
			
			// fileName이 null이 아니고 length()도 0이 아니라면
			// 업로드된 파일이 있는지 확인
			if (fileName != null && !fileName.isEmpty()) {
				part.write(fileName); // 서버에 파일 업로드
				// write()를 사용하기 위해서는 파일명이 필요하여 fileName을 얻음
				
				// 2. 경로를 포함한 이미지 파일 이름을 Board 객체에 저장
				b.setImg("/img/" + fileName);
			} else {
				b.setImg(null); // 업로드한 이미지가 없을 경우 null 저장
			}

			dao.insertBoard(b);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				// 쿼리스트링의 한글깨짐을 방지하기위해 UTF-8로 인코딩
				String encodeName = URLEncoder.encode("게시물이 정상적으로 등록되지 않았습니다.", "UTF-8");
				return "redirect:/index?error=" + encodeName;
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		} 
		
		return "redirect:/index";
	}
	
	// 게시물을 수정해준다.
	public String updateBoard(HttpServletRequest request) {
		Board b = new Board();
		String origin_file = request.getParameter("origin_file"); 
				
		try {
			BeanUtils.populate(b, request.getParameterMap());
			
			// 1. 이미지 파일을 서버(c:/Temp/img) 컴퓨터에 저장
			Part part = request.getPart("file"); // Part : 파일을 받기위한 클래스 타입
			String fileName = getFilename(part); // 파일에 대한 정보를 가지고 있는 part를 매개변수로 받는다.(파일명 얻음)
						
			// fileName이 null이 아니고 length()도 0이 아니라면
			// 업로드된 파일이 있는지 확인
			if (fileName != null && !fileName.isEmpty()) {
				part.write(fileName); // 서버에 파일 업로드
				// write()를 사용하기 위해서는 파일명이 필요하여 fileName을 얻음
				
				// 2. 경로를 포함한 이미지 파일 이름을 Board 객체에 저장
				b.setImg("/img/" + fileName);

			} else { // 업로드된 파일이 없을때
				if (origin_file == null || origin_file.equals("")) {
					b.setImg(null); // 업로드 된 파일이 없으면 null을 저장
				} else {
					b.setImg(origin_file);
				}
			}

			dao.updateBoard(b);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				// 쿼리스트링의 한글깨짐을 방지하기위해 UTF-8로 인코딩
				String encodeName = URLEncoder.encode("게시물이 정상적으로 수정되지 않았습니다.", "UTF-8");
				return "redirect:/view?board_no=" + b.getBoard_no() + "&error=" + encodeName;
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		} 
		
		return "redirect:/view?board_no=" + b.getBoard_no();
	}
	
	// 게시글을 삭제해준다.
	public String deleteBoard(HttpServletRequest request) {
		int board_no = Integer.parseInt(request.getParameter("board_no"));
		
		try {
			dao.deleteBoard(board_no);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				// 쿼리스트링의 한글깨짐을 방지하기위해 UTF-8로 인코딩
				String encodeName = URLEncoder.encode("게시물이 정상적으로 삭제되지 않았습니다.", "UTF-8");
				return "redirect:/index?error=" + encodeName;
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		
		return "redirect:/index";
	}
	
	// 파일에서 이미지명을 추출하는 메소드
	private String getFilename(Part part) {
		String fileName = null;
		
		// 파일 이름이 들어있는 헤더 영역을 가지고 옴
		String header = part.getHeader("content-disposition");
		System.out.println("Header => " + header); // form-data; name="img"; filename="사진5.jpg" <- 이런 형태로 찍힘
		
		// 파일 이름이 들어있는 속성 부분의 시작 위치(인덱스 번호)를 가지고 옴
		int start = header.indexOf("filename=");
		
		// 쌍따옴표 사이의 이미지명 부분만 가지고 옴
		fileName = header.substring(start + 10, header.length() - 1);
		
		return fileName;
	}
}
