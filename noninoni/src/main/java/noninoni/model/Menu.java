package noninoni.model;

import lombok.Data;

@Data
public class Menu {
	private String name;
	private String url;

	public Menu(String name, String url) {
		this.setName(name);
		this.setUrl(url);
	}

	
}