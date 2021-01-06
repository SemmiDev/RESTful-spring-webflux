package com.sammidev;

// © 2021 sammidev

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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
	private Path path;

	private Date createdAt;
	private Date updatedAt;
}

enum Path {
	SBMPTN,SNMPTN,MANDIRI
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class StudentCreateRequest {
	private String name;
	private String email;
	private String phone;
	private Path path;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class StudentUpdateRequest {
	private String name;
	private String email;
	private String phone;
}

interface StudentRepository extends ReactiveMongoRepository<Student, String> {}

@Component
class EmailValidator implements Predicate<String> {

	private static final Predicate<String> IS_EMAIL_VALID =
			Pattern.compile(
					"^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
					Pattern.CASE_INSENSITIVE
			).asPredicate();

	@Override
	public boolean test(String email) {
		return IS_EMAIL_VALID.test(email);
	}
}

@Component
class PhoneNumberValidator implements Predicate<String> {

	private static final Predicate<String> IS_PHONENUMBER_VALID = Pattern.compile("^\\d{12}$").asPredicate();
	@Override
	public boolean test(String phoneNumber) {
		return phoneNumber.startsWith("08") && IS_PHONENUMBER_VALID.test(phoneNumber);
	}
}

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
	public Mono<Student> createStudent(@Valid @RequestBody StudentCreateRequest student) {

		Calendar calendar = Calendar.getInstance();
		String uniqueNumber1 = String.format("%04d", new Random().nextInt(10000));
		String uniqueNumber2 = String.format("%04d", new Random().nextInt(10000));

		final String sbmptn = "311";
		final String snmptn = "312";
		final String mandiri = "313";

		String nim = null;
		if (student.getPath().toString().equals(Path.SBMPTN.name())) {
			nim = sbmptn + uniqueNumber1 + uniqueNumber2;
		}else if (student.getPath().toString().equals(Path.SNMPTN.name())) {
			nim = snmptn + uniqueNumber1 + uniqueNumber2;
		}else if (student.getPath().toString().equals(Path.MANDIRI.name())) {
			nim = snmptn + uniqueNumber1 + uniqueNumber2;
		}

		Student studentResult = null;

		if (new EmailValidator().test(student.getEmail()) && new PhoneNumberValidator().test(student.getPhone())) {
			studentResult = Student.builder()
					.name(student.getName())
					.nim(nim)
					.path(student.getPath())
					.email(student.getEmail())
					.phone(student.getPhone())
					.createdAt(new Date())
					.updatedAt(null)
					.build();
			return studentRepository.save(studentResult);
		}
		throw new IllegalArgumentException("phone number atau email anda tidak valid");
	}

	@PutMapping("/students/{id}")
	public Mono<ResponseEntity<Student>> updateStudent(@PathVariable(value = "id") String studentId,
												   @Valid @RequestBody StudentUpdateRequest studentRequest) {
		if (new EmailValidator().test(studentRequest.getEmail()) && new PhoneNumberValidator().test(studentRequest.getPhone())) {
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
		throw new IllegalArgumentException("phone number atau email anda tidak valid");
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