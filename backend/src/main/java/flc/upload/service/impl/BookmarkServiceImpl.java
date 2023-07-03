package flc.upload.service.impl;

import flc.upload.enums.BookmarkType;
import flc.upload.mapper.BookmarkMapper;
import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;
import flc.upload.service.BookmarkService;
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

//    private List<BookmarkVO> buildBookmarkVOs(List<Bookmark> bookmarks, int parentId) {
//
//        List<BookmarkVO> bookmarkVOs = new ArrayList<>();
//
//        for (Bookmark bookmark : bookmarks) {
//
//            if (bookmark.getParentId() == parentId) {
//
//                BookmarkVO bookmarkVO = new BookmarkVO();
//
//                bookmarkVO.setType(bookmark.getBookmarkTypeStr());
//                bookmarkVO.setName(bookmark.getName());
//
//
//                if (bookmark.isDirectory()) {
//                    bookmarkVO.setChildren(buildBookmarkVOs(bookmarks, bookmark.getId()));
//                } else {
//                    bookmarkVO.setUrl(bookmark.getUrl());
//                    bookmarkVO.setIcon(bookmarkVO.getIcon());
//                }
//                bookmarkVOs.add(bookmarkVO);
//            }
//        }
//        return bookmarkVOs;
//    }

    private List<BookmarkVO> buildBookmarkVOs(List<Bookmark> bookmarks, int parentId) {
        List<BookmarkVO> bookmarkVOs = new ArrayList<>();
        List<BookmarkVO> directoryVOs = new ArrayList<>();
        List<BookmarkVO> bookmarkItemVOs = new ArrayList<>();

        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getParentId() == parentId) {
                BookmarkVO bookmarkVO = new BookmarkVO();
                bookmarkVO.setType(bookmark.getBookmarkTypeStr());
                bookmarkVO.setName(bookmark.getName());

                if (bookmark.isDirectory()) {
                    bookmarkVO.setId(bookmark.getId());
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

