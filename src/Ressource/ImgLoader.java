package Ressource;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImgLoader {

	public Image getIconImage() {

		BufferedImage buff = null;
		try {
			buff = ImageIO.read(getClass().getResourceAsStream("Icon.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return buff;

	}

}
