package noninoni.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import noninoni.entity.Color;
import noninoni.repository.ColorRepository;

@Service
public class ColorService {

	@Autowired
	private ColorRepository colorRepository;

	public void addColor(String colorName, long colorNumber) {
		Color color = new Color();
		color.setName(colorName);
		color.setNumber(colorNumber);
		colorRepository.save(color);
	}

	public void deleteColor(Long id) {
		colorRepository.deleteById(id);
	}

	public void updateColor(Long id, String name, Long number) {
		Color color = colorRepository.findById(id).orElseThrow(() -> new RuntimeException("Color not found"));
		color.setName(name);
		color.setNumber(number);
		colorRepository.save(color);
	}

	 public List<Color> findAllSortedByNumber() {
        return colorRepository.findAll(Sort.by("number"));
    }
}
