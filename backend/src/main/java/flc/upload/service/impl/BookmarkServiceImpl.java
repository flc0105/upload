package flc.upload.service.impl;

import flc.upload.enums.BookmarkType;
import flc.upload.mapper.BookmarkMapper;
import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;
import flc.upload.model.Result;
import flc.upload.service.BookmarkService;
import flc.upload.util.ImageGenerator;
import flc.upload.util.JsoupUtil;
import org.springframework.stereotype.Service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkMapper bookmarkMapper;

    @Autowired
    public BookmarkServiceImpl(BookmarkMapper bookmarkMapper) {
        this.bookmarkMapper = bookmarkMapper;
    }

    @Override
    public void addBookmark(Bookmark bookmark) {
        bookmarkMapper.addBookmark(bookmark);
    }

    @Override
    public void deleteBookmarkById(Integer id) {
        Bookmark bookmark = bookmarkMapper.findById(id);
        if (bookmark.isDirectory()) {
            bookmarkMapper.deleteBookmarkByParentId(id);
        }
        bookmarkMapper.deleteBookmarkById(id);

    }

    @Override
    public void updateBookmark(Bookmark bookmark) {
        bookmarkMapper.updateBookmark(bookmark);
    }

    @Override
    public List<Bookmark> getBookmarksByParentId(Integer parentId) {
        return bookmarkMapper.getBookmarksByParentId(parentId);
    }

    @Override
    public List<Bookmark> getAllBookmarks() {
        return bookmarkMapper.getAllBookmarks();
    }

    @Override
    public List<BookmarkVO> getStructuredBookmarks() {
        List<Bookmark> bookmarks = bookmarkMapper.getAllBookmarks();
        return buildBookmarkVOs(bookmarks, 0);
    }

    @Override
    public void fetchBookmark(Integer id) {
        Bookmark bookmark = bookmarkMapper.findById(id);
        bookmark.setName(JsoupUtil.getTitle(bookmark.getUrl()));
        bookmarkMapper.updateBookmark(bookmark);
    }

    private List<BookmarkVO> buildBookmarkVOs(List<Bookmark> bookmarks, int parentId) {
        List<BookmarkVO> bookmarkVOs = new ArrayList<>();
        List<BookmarkVO> directoryVOs = new ArrayList<>();
        List<BookmarkVO> bookmarkItemVOs = new ArrayList<>();

        for (Bookmark bookmark : bookmarks) {
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
                    bookmarkVO.setIcon(bookmarkVO.getIcon());
                    bookmarkItemVOs.add(bookmarkVO);
                }
            }
        }

        bookmarkVOs.addAll(directoryVOs);
        bookmarkVOs.addAll(bookmarkItemVOs);
        return bookmarkVOs;
    }


}

