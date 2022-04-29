package flc.upload.service;

import flc.upload.mapper.TextMapper;
import flc.upload.model.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TextService {
    @Autowired
    TextMapper textMapper;

    public List<Text> findAll() {
        return textMapper.findAll();
    }

    public Integer add(Text text) throws Exception {
        return textMapper.add(text);
    }

    public Integer delete(Integer id) {
        return textMapper.delete(id);
    }
}
