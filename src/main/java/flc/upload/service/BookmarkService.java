package flc.upload.service;

import flc.upload.mapper.BookmarkMapper;
import flc.upload.model.Bookmark;
import flc.upload.model.Result;
import flc.upload.util.JsoupUtil;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class BookmarkService {
    private BookmarkMapper bookmarkMapper;

    public BookmarkService(BookmarkMapper bookmarkMapper) {
        this.bookmarkMapper = bookmarkMapper;
    }

    public Result add(String url) throws Exception {
        Bookmark bookmark = new Bookmark();
        bookmark.setUrl(url.contains("://") ? url : "http://" + url);
        return new Result<>(bookmarkMapper.add(bookmark) != 0, null, bookmark.getId());
    }

    public Result delete(Integer id) throws Exception {
        return new Result<>(bookmarkMapper.delete(id) != 0, null);
    }

    public Result update(Integer id) throws Exception {
        Bookmark bookmark = bookmarkMapper.findById(id);
        bookmark.setTitle(JsoupUtil.getTitle(bookmark.getUrl()));
        String iconUrl = JsoupUtil.getIcon(bookmark.getUrl());
        if (iconUrl != null) {
            bookmark.setIcon(JsoupUtil.fileToBase64(iconUrl));
        } else {
            URL url = new URL(bookmark.getUrl());
            bookmark.setIcon(JsoupUtil.fileToBase64(url.getProtocol() + "://" + url.getAuthority() + "/favicon.ico"));
        }
        return new Result<>(bookmarkMapper.update(bookmark) != 0, null);
    }

    public Result findAll() throws Exception {
        return new Result<>(true, null, bookmarkMapper.findAll());
    }
}
