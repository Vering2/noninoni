package noninoni.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import noninoni.entity.Size;
import noninoni.repository.SizeRepository;


@Service
public class SizeService {
	
	@Autowired
	private SizeRepository sizeRepository;
	

	public void deleteSize(Long id) {
		// TODO Auto-generated method stub
		sizeRepository.deleteById(id);
	}

	
	public void addSize(String sizeName, long sizeNumber) {
		Size size = new Size();
		size.setName(sizeName);
		size.setNumber(sizeNumber);
		sizeRepository.save(size);
	}

	public void updateSize(Long id, String name, Long number) {
    Size size = sizeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Size not found"));
    size.setName(name);
    size.setNumber(number);
    sizeRepository.save(size);
}
	public List<Size> findAllSortedByNumber() {
        return sizeRepository.findAll(Sort.by("number"));
    }




}
