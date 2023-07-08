package flc.upload.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flc.upload.enums.BookmarkType;
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


    /**
     * 根据ID删除书签及其子目录（如果是目录）
     *
     * @param id 书签ID
     */
    @Override
    public void deleteById(Integer id) {
        Bookmark bookmark = bookmarkMapper.selectById(id);
        if (bookmark != null) {
            if (bookmark.isDirectory()) {
                bookmarkMapper.delete(new LambdaQueryWrapper<Bookmark>().eq(Bookmark::getParentId, id));
            }
            bookmarkMapper.deleteById(id);
        }
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

}

