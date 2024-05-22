package com.example.server.domain.image.service;

import com.example.server.domain.image.dto.ImageResponseDto.UploadResponseDto;
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
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
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
    public static final String DEFAULT_URI_PREFIX = "https://" + BUCKET_NAME_SPACE + ".objectstorage."
            + Region.AP_CHUNCHEON_1.getRegionId() + ".oci.customer-oci.com";
    private static final String POST_IMG_DIR = "post/";


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
    public UploadResponseDto uploadProfileImg(MultipartFile file, String memberId) throws Exception {
        File uploadFile = convert(file)  // 파일 변환할 수 없으면 에러
                .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        String fileDir = member.getIdx() + "/" + PROFILE_IMG_DIR;
        return upload(uploadFile, fileDir);
    }

    @Override
    public UploadResponseDto uploadImg(MultipartFile file, String memberId) throws Exception{
        File uploadFile = convert(file)  // 파일 변환할 수 없으면 에러
                .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        String fileDir = member.getIdx() + "/" + POST_IMG_DIR;
        return upload(uploadFile, fileDir);
    }

    @Override
    public MultipartFile downloadImg(Long imageIdx, Long memberIdx) throws Exception{
        return null;
    }

    // 버킷에서 이미지와 인증정보 삭제
    @Override
    public void deleteImg(Long idx) throws Exception {
//        ObjectStorage client = getClient();
//        Image img = imageRepository.findImageByIdx(idx).orElseThrow(
//                () -> new ErrorHandler(ErrorStatus.IMAGE_NOT_FOUND)
//        );
//        DeleteObjectRequest request =
//                DeleteObjectRequest.builder()
//                        .bucketName(BUCKET_NAME)
//                        .namespaceName(BUCKET_NAME_SPACE)
//                        .objectName(img.getImgUrl())
//                        .build();
//
//        deletePreAuth(img.getParId());
//
//        client.deleteObject(request);
//        client.close();
//
//        imageRepository.delete(img);
    }

    // 오라클 버킷으로 파일 업로드
    public UploadResponseDto upload(File uploadFile, String dirName) throws Exception {
        ObjectStorage client = getClient();
        UploadManager uploadManager = getManager(client);

        String fileName = dirName + uploadFile.getName();   // 버킷에 저장된 파일 이름
        String contentType = "img/" + fileName.substring(fileName.length() - 3); // PNG, JPG 만 가능함
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
                        .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectReadWrite)
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


    // 로컬에 파일 업로드 해서 convert
    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(System.getProperty("user.home") + "/rideTogetherDummy/" + UUID.randomUUID() +file.getOriginalFilename());
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
