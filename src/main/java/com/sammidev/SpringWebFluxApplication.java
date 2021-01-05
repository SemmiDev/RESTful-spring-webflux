package com.sammidev;

// © 2021 sammidev

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;

@SpringBootApplication
public class SpringWebFluxApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringWebFluxApplication.class, args);
	}
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "student_registration")
class Student {
	@Id
 	private String id;

	private String name;
	private String nim;
	private String email;
	private String phone;

	private Date createdAt;
	private Date updatedAt;

	public Student(String name, String nim, String email, String phone, Date createdAt, Date updatedAt) {
		this.name = name;
		this.nim = nim;
		this.email = email;
		this.phone = phone;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}

interface StudentRepository extends ReactiveMongoRepository<Student, String> {}

@RestController
class StudentController {

	@Autowired
	private StudentRepository studentRepository;

	@GetMapping("/students")
	public Flux<Student> getAllStudents() {
		return studentRepository.findAll();
	}

	@GetMapping("/students/{id}")
	public Mono<ResponseEntity<Student>> getStudentById(@PathVariable(value = "id") String studentId) {
		return studentRepository.findById(studentId)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping("/students")
	public Mono<Student> createStudent(@Valid @RequestBody Student student) {
		var studentUpload = new Student(
				student.getName(),
				nimgenerator(),
				student.getEmail(),
				student.getPhone(),
				new Date(),
				null);
		return studentRepository.save(studentUpload);
	}

	private String nimgenerator() {
		LocalDate date = LocalDate.now();
		var format = date.format(DateTimeFormatter.BASIC_ISO_DATE);
		return format + new Date().toString();
	}

	@PutMapping("/students/{id}")
	public Mono<ResponseEntity<Student>> updateStudent(@PathVariable(value = "id") String studentId,
												   @Valid @RequestBody Student studentRequest) {
		return studentRepository.findById(studentId)
				.flatMap(existingStudent -> {
					existingStudent.setName(studentRequest.getName());
					existingStudent.setEmail(studentRequest.getEmail());
					existingStudent.setPhone(studentRequest.getPhone());
					existingStudent.setUpdatedAt(new Date());
					return studentRepository.save(existingStudent);
				})
				.map(updatedStudent -> new ResponseEntity<>(updatedStudent, OK))
				.defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
	}

	@DeleteMapping("/students/{id}")
	public Mono<ResponseEntity<Void>> deleteStudent(@PathVariable(value = "id") String studentId) {
		return studentRepository.findById(studentId)
				.flatMap(existingStudent ->
						studentRepository.delete(existingStudent)
								.then(Mono.just(new ResponseEntity<Void>(OK)))
				)
				.defaultIfEmpty(new ResponseEntity<>(NOT_FOUND));
	}

	@GetMapping(value = "/stream/students", produces = TEXT_EVENT_STREAM_VALUE)
	public Flux<Student> streamAllStudents() {
		return studentRepository.findAll();
	}
}