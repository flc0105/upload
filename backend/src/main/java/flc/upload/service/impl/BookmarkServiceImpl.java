package flc.upload.service.impl;

import flc.upload.mapper.BookmarkMapper;
import flc.upload.model.Bookmark;
import flc.upload.model.Result;
import flc.upload.model.Tag;
import flc.upload.service.BookmarkService;
import flc.upload.util.ImageGenerator;
import flc.upload.util.JsoupUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookmarkServiceImpl implements BookmarkService {
    private BookmarkMapper bookmarkMapper;

    public BookmarkServiceImpl(BookmarkMapper bookmarkMapper) {
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
            String icon = JsoupUtil.fileToBase64(url.getProtocol() + "://" + url.getAuthority() + "/favicon.ico");
            if (icon.isEmpty()) {
                icon = ImageGenerator.generateImageBase64(bookmark.getTitle().charAt(0));
            }
            bookmark.setIcon(icon);
        }
        return new Result<>(bookmarkMapper.update(bookmark) != 0, null);
    }

    public Result findAll() throws Exception {

        List<Bookmark> bookmarks = bookmarkMapper.findAll();

        for (Bookmark bookmark : bookmarks) {
            List<Tag> tags = bookmarkMapper.getTagsForBookmark(bookmark.getId());
            bookmark.setTags(tags);
        }

//        return new Result<>(true, null, bookmarkMapper.findAll());
        return new Result<>(true, "查询成功", bookmarks);
    }

    public Result bulkAdd(String data) throws Exception {
        JSONArray jsonArray = JSONArray.fromObject(data);
        int i = 0;
        for (Object str : jsonArray) {
            JSONObject jsonObject = JSONObject.fromObject(str);
            String title = jsonObject.getString("title");
            String url = jsonObject.getString("url");
            Bookmark bookmark = new Bookmark();
            bookmark.setTitle(title);
            bookmark.setUrl(url.contains("://") ? url : "http://" + url);
            if (bookmarkMapper.add(bookmark) != 0) {
                i++;
            }
        }
        return new Result(true, "成功导入" + i + "条数据");
    }

    public Result updateAll() throws Exception {
        List<Bookmark> bookmarks = bookmarkMapper.findAll();
        int i = 0;
        for (Bookmark bookmark : bookmarks) {
            if (update(bookmark.getId()).isSuccess()) {
                i++;
            }
        }
        return new Result(true, "成功更新" + i + "条数据");
    }

    public Result rename(Integer id, String title, String url) throws Exception {
        Bookmark bookmark = bookmarkMapper.findById(id);
        if (title != null) {
            bookmark.setTitle(title);
        }
        if (url != null) {
            bookmark.setUrl(url);
        }
        return new Result<>(bookmarkMapper.update(bookmark) != 0, null);
    }

    @Override
    public Result addTags(Integer bookmarkId, List<Integer> tagIds) throws Exception {

        if (tagIds.isEmpty()) {
            return new Result<>(true, "没有添加标签", null);
        }

        for (Integer id : tagIds) {
            bookmarkMapper.addTag(bookmarkId, id);

        }
        return new Result<>(true, "添加完成", null);
    }

    public Result findAllTags() throws Exception {
        return new Result<>(true, "查询完成", bookmarkMapper.findAllTags());
    }

    public Result findBookmarksByTags(List<Integer> tagIds) throws Exception {

        String idString = tagIds.stream().map(Object::toString).collect(Collectors.joining(","));
        System.out.println(idString);
        List<Bookmark> bookmarks = bookmarkMapper.findBookmarksByTags(tagIds, idString);


        for (Bookmark bookmark : bookmarks) {
            List<Tag> tags = bookmarkMapper.getTagsForBookmark(bookmark.getId());
            bookmark.setTags(tags);
        }

//        return new Result<>(true, null, bookmarkMapper.findAll());
        return new Result<>(true, null, bookmarks);
    }

}
