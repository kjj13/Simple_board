package hello.board.controller;

import hello.board.domain.entity.Board;
import hello.board.dto.BoardDto;
import hello.board.dto.FileDto;
import hello.board.service.BoardService;
import hello.board.service.FileService;
import hello.board.util.MD5Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final FileService fileService;

    @GetMapping("/")
    public String list(Model model){
        List<BoardDto> boardDtoList = boardService.getBoardList();
        model.addAttribute("postList",boardDtoList);
        return "board/list";
    }

    @GetMapping("/post")
    public String post(){
        return "board/post";
    }

    @PostMapping("/post")
    public String write(@RequestParam("file") MultipartFile files, BoardDto boardDto) {
        try {
            String origFilename = files.getOriginalFilename();
            String filename = new MD5Generator(origFilename).toString();
            /* 실행되는 위치의 'files' 폴더에 파일이 저장됩니다. */
//            window 파일 경로
//            String savePath = System.getProperty("user.dir") + "\\files";
//            mac 파일 경로
            String savePath = System.getProperty("user.dir") + "/files";
            /* 파일이 저장되는 폴더가 없으면 폴더를 생성합니다. */
            if (!new File(savePath).exists()) {
                try{
                    new File(savePath).mkdir();
                }
                catch(Exception e){
                    e.getStackTrace();
                }
            }
//            window 파일 경로
//            String filePath = savePath + "\\" + filename;
//            mac 파일 경로
            String filePath = savePath + "/" + filename;
            files.transferTo(new File(filePath));

            FileDto fileDto = new FileDto();
            fileDto.setOrigFilename(origFilename);
            fileDto.setFilename(filename);
            fileDto.setFilePath(filePath);

            Long fileId = fileService.saveFile(fileDto);
            boardDto.setFileId(fileId);
            boardService.savePost(boardDto);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/post/{id}")
    public String detail(@PathVariable("id") Long id,Model model){
        BoardDto boardDto = boardService.getPost(id);
        model.addAttribute("post",boardDto);

        FileDto fileDto = fileService.getFile(boardDto.getFileId());
        String fileName = fileDto.getOrigFilename();
        model.addAttribute("filename",fileName);
        return "/board/detail";
    }

    @GetMapping("/post/edit/{id}")
    public String edit(@PathVariable("id") Long id,Model model){
        BoardDto boardDto = boardService.getPost(id);
        model.addAttribute("post",boardDto);
        return "/board/edit";
    }

    @PutMapping("/post/edit/{id}")
    public String update(BoardDto boardDto){
        boardService.savePost(boardDto);
        return "redirect:/";    // redirect:보내고싶은 주소
    }

    @DeleteMapping("/post/{id}")
    public String delete(@PathVariable("id") Long id){
        BoardDto boardDto = boardService.getPost(id);   // 이걸 먼저 했어야 했는데 ㅎㅎ. 삭제하고 찾으니 오류가 뜨지 중재야.
        boardService.delete(id);
        fileService.delete(boardDto.getFileId());
        return "redirect:/";
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> fileDownload(@PathVariable("fileId") Long fileId) throws IOException {
        FileDto fileDto = fileService.getFile(fileId);
        Path path = Paths.get(fileDto.getFilePath());
        Resource resource = new InputStreamResource(Files.newInputStream(path));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getOrigFilename() + "\"")
                .body(resource);
    }
}
