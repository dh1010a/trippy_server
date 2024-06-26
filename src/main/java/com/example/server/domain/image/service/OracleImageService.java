package com.example.server.domain.image.service;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.dto.ImageDto;
import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.image.dto.ImageResponseDto.UpdateImageResponseDto;
import com.example.server.domain.image.dto.ImageResponseDto.UploadResponseDto;
import com.example.server.domain.image.model.ImageType;
import com.example.server.domain.image.repository.ImageRepository;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.ConfigFileReader.ConfigFile;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.model.PreauthenticatedRequestSummary;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import com.oracle.bmc.objectstorage.responses.ListPreauthenticatedRequestsResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OracleImageService implements ImageService {

    private final MemberRepository memberRepository;

    private final ImageRepository imageRepository;



    private static final String BUCKET_NAME = "RideTogetherHYU_Bucket";
    private static final String BUCKET_NAME_SPACE = "axjoaeuyezzj";
    private static final String PROFILE_IMG_DIR = "profile/";
    private static final String BLOG_IMG_DIR = "blog/";
    private static final String DEFAULT_IMG_DIR = "img/";
    public static final String DEFAULT_URI_PREFIX = "https://" + BUCKET_NAME_SPACE + ".objectstorage."
            + Region.AP_CHUNCHEON_1.getRegionId() + ".oci.customer-oci.com";


    public ObjectStorage getClient() throws Exception {
        ConfigFile config = ConfigFileReader.parse("~/.oci/config", "DEFAULT");

        AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(config);

        return ObjectStorageClient.builder()
                .region(Region.AP_CHUNCHEON_1)
                .build(provider);
    }

    public UploadManager getManager(ObjectStorage client) throws Exception {
        UploadConfiguration configuration = UploadConfiguration.builder()
                .allowMultipartUploads(true)
                .allowParallelUploads(true)
                .build();
        return new UploadManager(client, configuration);
    }

    public UploadConfiguration getUploadConfiguration() {
        //upload object
        return UploadConfiguration.builder()
                .allowMultipartUploads(true)
                .allowParallelUploads(true)
                .build();
    }


    @Override
    public UploadResponseDto uploadImg(MultipartFile file, String memberId) throws Exception{
        File uploadFile = convert(file)  // 파일 변환할 수 없으면 에러
                .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        String fileDir = member.getIdx() + "/" + DEFAULT_IMG_DIR;
        log.info("사진 업로드 요청. MemberId : {}", memberId);
        return upload(uploadFile, fileDir);
    }

    @Override
    public MultipartFile downloadImg(Long imageIdx, Long memberIdx) throws Exception{
        return null;
    }

    // 버킷에서 이미지와 인증정보 삭제
    @Override
    public void deleteImg(Long id) throws Exception {
        ObjectStorage client = getClient();
        Image img = imageRepository.findById(id).orElseThrow(
                () -> new ErrorHandler(ErrorStatus.IMAGE_NOT_FOUND)
        );
        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucketName(BUCKET_NAME)
                        .namespaceName(BUCKET_NAME_SPACE)
                        .objectName(img.getImgUrl())
                        .build();

        deletePreAuth(img.getAuthenticateId());

        client.deleteObject(request);
        client.close();

        imageRepository.delete(img);
        log.info("이미지 삭제에 성공하였습니다. 이미지 ID : {}", id);
    }

    @Override
    public void deleteImg(ImageDto imageDto) throws Exception {
        ObjectStorage client = getClient();
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucketName(BUCKET_NAME)
                .namespaceName(BUCKET_NAME_SPACE)
                .objectName(imageDto.getImgUrl())
                .build();

        deletePreAuth(imageDto.getAuthenticateId());

        client.deleteObject(request);
        client.close();
        log.info("이미지 삭제에 성공하였습니다. 이미지 URL : {}", imageDto.getImgUrl());
    }

    // 오라클 버킷으로 파일 업로드
    public UploadResponseDto upload(File uploadFile, String dirName) throws Exception {
        ObjectStorage client = getClient();
        UploadManager uploadManager = getManager(client);

        String fileName = dirName + uploadFile.getName();   // 버킷에 저장된 파일 이름
        String contentType = fileName.lastIndexOf(".") == 4 ? "img/" + fileName.substring(fileName.length() - 4)
                : "img/" + fileName.substring(fileName.length() - 3);
        PutObjectRequest request = PutObjectRequest.builder()
                        .bucketName(BUCKET_NAME)
                        .namespaceName(BUCKET_NAME_SPACE)
                        .objectName(fileName)
                        .contentType(contentType)
                        .build();
        UploadRequest uploadDetails = UploadRequest.builder(uploadFile).allowOverwrite(true).build(request);
        UploadResponse response = uploadManager.upload(uploadDetails);

        client.close();
        log.info("Upload Success. File : {}", fileName);

        removeNewFile(uploadFile);
        return getPublicImgUrl(fileName);
    }

    public UploadResponseDto getPublicImgUrl(String fileName) throws Exception {
        ObjectStorage client = getClient();
        UploadResponseDto authenticatedResponse = getPreAuth(fileName);

        log.info("PublicImgUrl 발급에 성공하였습니다 : {}", authenticatedResponse.getAccessUri());
        client.close();
        return authenticatedResponse;
    }

    public UploadResponseDto getPreAuth(String imgUrl) throws Exception{
        ObjectStorage client = getClient();

        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.DECEMBER, 30);

        Date expireTime = cal.getTime();

        log.info("권한을 얻어오기 위해 시도중입니다. 파일 이름 : {}", imgUrl);
        CreatePreauthenticatedRequestDetails details =
                CreatePreauthenticatedRequestDetails.builder()
                        .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                        .objectName(imgUrl)
                        .timeExpires(expireTime)
                        .name(imgUrl)
                        .build();

        CreatePreauthenticatedRequestRequest request =
                CreatePreauthenticatedRequestRequest.builder()
                        .namespaceName(BUCKET_NAME_SPACE)
                        .bucketName(BUCKET_NAME)
                        .createPreauthenticatedRequestDetails(details)
                        .build();

        CreatePreauthenticatedRequestResponse response = client.createPreauthenticatedRequest(request);
        client.close();
        return UploadResponseDto.builder()
                .authenticateId(response.getPreauthenticatedRequest().getId())
                .imgUrl(imgUrl)
                .accessUri(DEFAULT_URI_PREFIX + response.getPreauthenticatedRequest().getAccessUri())
                .build();
    }

    public String deleteAllPreAuth() throws Exception {
        ObjectStorage client = getClient();

        ListPreauthenticatedRequestsRequest listPreauthenticatedRequestsRequest
                = ListPreauthenticatedRequestsRequest.builder()
                .namespaceName(BUCKET_NAME_SPACE)
                .bucketName(BUCKET_NAME)
                .build();

        ListPreauthenticatedRequestsResponse response = client.listPreauthenticatedRequests(listPreauthenticatedRequestsRequest);
        log.info("response : {}", response.getItems());
        List<PreauthenticatedRequestSummary> items = response.getItems();
        for (PreauthenticatedRequestSummary item : items) {
            System.out.println("item = " + item);
            deletePreAuth(item.getId());
        }
        client.close();
        return "모든 사전인증 요청이 삭제되었습니다.";
    }

    public String deleteAllImage() throws Exception {
        ObjectStorage client = getClient();
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .namespaceName(BUCKET_NAME_SPACE)
                .bucketName(BUCKET_NAME)
                .build();

        ListObjectsResponse response = client.listObjects(listObjectsRequest);
        List<ObjectSummary> objects = response.getListObjects().getObjects();
        for (ObjectSummary object : objects) {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .namespaceName(BUCKET_NAME_SPACE)
                    .bucketName(BUCKET_NAME)
                    .objectName(object.getName())
                    .build();
            client.deleteObject(request);
        }
        client.close();
        return "모든 이미지가 삭제되었습니다.";
    }



    private void deletePreAuth(String parId) throws Exception {
        ObjectStorage client = getClient();
        DeletePreauthenticatedRequestRequest request =
                DeletePreauthenticatedRequestRequest.builder()
                        .namespaceName(BUCKET_NAME_SPACE)
                        .bucketName(BUCKET_NAME)
                        .parId(parId)
                        .build();

        client.deletePreauthenticatedRequest(request);
        client.close();
    }



    // 로컬에 파일 업로드 해서 convert
    private Optional<File> convert(MultipartFile file) throws IOException {
        String directoryPath = System.getProperty("user.home") + "/rideTogetherDummy/";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs(); // 디렉토리 생성
        }

        File convertFile = new File(System.getProperty("user.home") + "/rideTogetherDummy/" + UUID.randomUUID() +file.getOriginalFilename());
        System.out.println("System.getProperty = " + System.getProperty("user.home"));
        if (convertFile.createNewFile()) { // 바로 위에서 지정한 경로에 File이 생성됨 (경로가 잘못되었다면 생성 불가능)
            try (FileOutputStream fos = new FileOutputStream(
                    convertFile)) { // FileOutputStream 데이터를 파일에 바이트 스트림으로 저장하기 위함
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    // 로컬에 저장된 이미지 지우기
    @Override
    public void removeNewFile(File targetFile) {
        log.info("@@@@@@@@ 지울 대상 파일 이름"+targetFile.getName());
        log.info("@@@@@@@@ 지울 대상 파일 경로"+targetFile.getPath());
        if (targetFile.exists()) {
            if (targetFile.delete()) {
                log.info("@@@@@@@@ File delete success");
                return;
            }
            log.info("@@@@@@@@ File delete fail.");
        }
        log.info("@@@@@@@@ File not exist.");
    }




}
