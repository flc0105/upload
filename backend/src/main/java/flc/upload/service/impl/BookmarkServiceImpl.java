package flc.upload.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flc.upload.enums.BookmarkType;
import flc.upload.exception.BusinessException;
import flc.upload.mapper.BookmarkMapper;
import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;
import flc.upload.service.BookmarkService;
import flc.upload.util.JsoupUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    @Autowired
    public BookmarkServiceImpl(BookmarkMapper bookmarkMapper) {
        this.bookmarkMapper = bookmarkMapper;
    }

    /**
     * 检查目录是否已存在
     *
     * @param bookmark 目录书签
     * @return 如果目录已存在返回 true，否则返回 false
     */
    private boolean directoryAlreadyExists(Bookmark bookmark) {
        return bookmarkMapper.selectCount(
                new LambdaQueryWrapper<Bookmark>()
                        .eq(Bookmark::getName, bookmark.getName())
                        .eq(Bookmark::getParentId, bookmark.getParentId())
                        .eq(Bookmark::getBookmarkType, BookmarkType.DIRECTORY.getValue())
        ) > 0;
    }

    /**
     * 添加书签或目录
     *
     * @param bookmark 书签或目录对象
     * @throws BusinessException 如果目录名称非法或目录已存在，将抛出业务异常
     */
    @Override
    public void add(Bookmark bookmark) {
        if (bookmark.isDirectory()) {
            // 检查目录名称是否非法
            if (bookmark.isDirectoryNameInvalid()) {
                throw new BusinessException("directory.name.illegal");
            }

            // 检查目录是否已存在
            if (directoryAlreadyExists(bookmark)) {
                throw new BusinessException("directory.already.exists");
            }
        }

        if (bookmark.isBookmark()) {
            // 设置书签的 URL
            bookmark.setUrl(bookmark.getUrl());
        }

        // 设置书签或目录的名称
        bookmark.setName(bookmark.getName());

        // 插入书签或目录到数据库
        bookmarkMapper.insert(bookmark);
    }


    public void deleteById(Integer id) {
        deleteRecursive(id);
    }

    private void deleteRecursive(Integer id) {
        List<Bookmark> childBookmarks = bookmarkMapper.selectList(new LambdaQueryWrapper<Bookmark>().eq(Bookmark::getParentId, id));
        for (Bookmark childBookmark : childBookmarks) {
            if (childBookmark.isDirectory()) {
                deleteRecursive(childBookmark.getId());
            }
            bookmarkMapper.deleteById(childBookmark.getId());
        }
        bookmarkMapper.deleteById(id);
    }


    /**
     * 更新书签或目录
     *
     * @param bookmark 待更新的书签或目录对象
     * @throws BusinessException 如果目录名称非法或目录已存在，将抛出业务异常
     */
    @Override
    public void updateBookmark(Bookmark bookmark) {
        Bookmark existingBookmark = bookmarkMapper.selectById(bookmark.getId());
        if (existingBookmark != null) {
            if (existingBookmark.isDirectory()) {
                String newName = bookmark.getName();
                if (bookmark.isDirectoryNameInvalid()) {
                    throw new BusinessException("directory.name.illegal");
                }
                if (bookmarkMapper.selectList(new LambdaQueryWrapper<Bookmark>()
                        .eq(Bookmark::getParentId, existingBookmark.getParentId())
                        .eq(Bookmark::getName, newName)
                        .eq(Bookmark::getBookmarkType, BookmarkType.DIRECTORY.getValue())
                        .ne(Bookmark::getId, existingBookmark.getId())
                ).size() > 0) {
                    throw new BusinessException("directory.already.exists");
                }
            }
            existingBookmark.updatePropertiesFromBookmark(bookmark);
            bookmarkMapper.updateById(existingBookmark);
        }
    }


    @Override
    public List<BookmarkVO> getStructuredBookmarks() {
        List<Bookmark> bookmarks = bookmarkMapper.selectList(null);
        return buildBookmarkVOs(bookmarks, 0);
    }

    @Override
    public void updateBookmarkWithParsedData(Integer id) {
        Bookmark bookmark = bookmarkMapper.selectById(id);
        bookmark.setName(JsoupUtil.getTitle(bookmark.getUrl()));
        bookmark.setIcon(JsoupUtil.getIcon(bookmark.getUrl()));
        bookmarkMapper.updateById(bookmark);
    }


    private List<BookmarkVO> buildBookmarkVOs(List<Bookmark> bookmarks, int parentId) {
        List<BookmarkVO> bookmarkVOs = new ArrayList<>();
        List<BookmarkVO> directoryVOs = new ArrayList<>();
        List<BookmarkVO> bookmarkItemVOs = new ArrayList<>();
        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getParentId() == null) {
                continue;
            }
            if (bookmark.getParentId() == parentId) {
                BookmarkVO bookmarkVO = new BookmarkVO();
                bookmarkVO.setType(bookmark.getBookmarkTypeStr());
                bookmarkVO.setName(bookmark.getName());
                bookmarkVO.setId(bookmark.getId());
                if (bookmark.isDirectory()) {
                    bookmarkVO.setChildren(buildBookmarkVOs(bookmarks, bookmark.getId()));
                    directoryVOs.add(bookmarkVO);
                } else {
                    bookmarkVO.setUrl(bookmark.getUrl());
                    bookmarkVO.setIcon(bookmark.getIcon());
                    bookmarkItemVOs.add(bookmarkVO);

                }
            }
        }
        bookmarkVOs.addAll(directoryVOs);
        bookmarkVOs.addAll(bookmarkItemVOs);
        return bookmarkVOs;
    }


    @Override
    public void importBookmarks(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<BookmarkVO> bookmarkVOs = mapper.readValue(json, new TypeReference<List<BookmarkVO>>() {
            });
            for (BookmarkVO bookmarkVO : bookmarkVOs) {
                saveBookmark(bookmarkVO, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBookmark(BookmarkVO bookmarkVO, Integer parentId) {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(bookmarkVO.getId());
        bookmark.setName(bookmarkVO.getName());
        bookmark.setParentId(parentId);
        bookmark.setUrl(bookmarkVO.getUrl());
        bookmark.setIcon(bookmarkVO.getIcon());
        bookmark.setBookmarkType(Objects.equals(bookmarkVO.getType(), "bookmark") ? BookmarkType.BOOKMARK.getValue() : BookmarkType.DIRECTORY.getValue());

        bookmarkMapper.insert(bookmark);

        List<BookmarkVO> children = bookmarkVO.getChildren();
        if (children != null && !children.isEmpty()) {
            for (BookmarkVO child : children) {
                saveBookmark(child, bookmark.getId());
            }
        }
    }

    @Override
    public void exportBookmarksToExcel(HttpServletResponse response) {
        List<Bookmark> bookmarks = bookmarkMapper.selectList(new LambdaQueryWrapper<Bookmark>().eq(Bookmark::getBookmarkType, 1));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Bookmarks");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            Cell headerCell1 = headerRow.createCell(0);
            headerCell1.setCellValue("Name");
            Cell headerCell2 = headerRow.createCell(1);
            headerCell2.setCellValue("URL");

            // 填充数据
            int rowIndex = 1;
            for (Bookmark bookmark : bookmarks) {
                Row dataRow = sheet.createRow(rowIndex++);
                Cell dataCell1 = dataRow.createCell(0);
                dataCell1.setCellValue(bookmark.getName());
                Cell dataCell2 = dataRow.createCell(1);
                dataCell2.setCellValue(bookmark.getUrl());
            }

            // 调整列宽
            for (int columnIndex = 0; columnIndex < 2; columnIndex++) {
                sheet.autoSizeColumn(columnIndex);
            }

            // 保存Excel文件
//            try (FileOutputStream fileOutputStream = new FileOutputStream("bookmarks.xlsx")) {
//                workbook.write(fileOutputStream);
//            }
//            FileUtil.download(new File("bookmarks.xlsx"), response);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"bookmarks.xlsx\"");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            // 将Excel数据写入响应输出流
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

