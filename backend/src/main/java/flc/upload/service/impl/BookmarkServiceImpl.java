package flc.upload.service.impl;

import flc.upload.exception.BusinessException;
import flc.upload.mapper.BookmarkMapper;
import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;
import flc.upload.service.BookmarkService;
import flc.upload.util.JsoupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void addBookmark(Bookmark bookmark) {
        if (bookmark.isDirectory()) {
            String newName = bookmark.getName();
            if (!bookmark.isValidName()) {
                throw new BusinessException("directory.name.illegal");
            }
            if (!bookmarkMapper.findByName(newName, bookmark.getParentId()).isEmpty()) {
                throw new BusinessException("directory.already.exists");
            }
        }
        if (bookmark.isBookmark()) {
            bookmark.setUrl(bookmark.getUrl().contains("://") ? bookmark.getUrl() : "http://" + bookmark.getUrl());
        }
        bookmark.setName(bookmark.getName() != null ? bookmark.getName().trim() : null);
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
        Bookmark existingBookmark = bookmarkMapper.findById(bookmark.getId());
        if (existingBookmark.isDirectory()) {
            String newName = bookmark.getName();
            if (!bookmark.isValidName()) {
                throw new BusinessException("directory.name.illegal");
            }
            if (!bookmarkMapper.findByName(newName, existingBookmark.getParentId()).isEmpty() && !Objects.equals(existingBookmark.getName(), newName)) {
                throw new BusinessException("directory.already.exists");
            }
        }
        existingBookmark.copyFrom(bookmark);
        existingBookmark.setName(existingBookmark.getName().trim());
        bookmarkMapper.updateBookmark(existingBookmark);
    }

    @Override
    public List<BookmarkVO> getStructuredBookmarks() {
        List<Bookmark> bookmarks = bookmarkMapper.getAllBookmarks();
        return buildBookmarkVOs(bookmarks, 0);
    }

    @Override
    public void fetchBookmarkTitle(Integer id) {
        Bookmark bookmark = bookmarkMapper.findById(id);
        bookmark.setName(JsoupUtil.getTitle(bookmark.getUrl()));
        bookmarkMapper.updateBookmark(bookmark);
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

