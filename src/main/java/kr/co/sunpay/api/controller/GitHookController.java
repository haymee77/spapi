package kr.co.sunpay.api.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/git")
public class GitHookController {

	/**
	 * Gitlab 에서 Push event 발생 시 호출되는 API
	 * 
	 * @param project
	 * @param body
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/push/{project}")
	public ResponseEntity<Object> pushListener(@PathVariable("project") String project,
			@RequestBody Map<String, Object> body) throws IOException {
		
		try {
			System.out.println("pushListener...");
			String command = "sh";
			String path = "/home/ubuntu/app/" + project + "/deploy.sh";
			ProcessBuilder pb = new ProcessBuilder(command, path);
			pb.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		return ResponseEntity.ok().build();
	}
}