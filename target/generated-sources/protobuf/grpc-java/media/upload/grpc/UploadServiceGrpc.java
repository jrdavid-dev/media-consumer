package media.upload.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.71.0)",
    comments = "Source: upload.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class UploadServiceGrpc {

  private UploadServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "media.upload.UploadService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<media.upload.grpc.UploadRequest,
      media.upload.grpc.UploadResponse> getUploadMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Upload",
      requestType = media.upload.grpc.UploadRequest.class,
      responseType = media.upload.grpc.UploadResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<media.upload.grpc.UploadRequest,
      media.upload.grpc.UploadResponse> getUploadMethod() {
    io.grpc.MethodDescriptor<media.upload.grpc.UploadRequest, media.upload.grpc.UploadResponse> getUploadMethod;
    if ((getUploadMethod = UploadServiceGrpc.getUploadMethod) == null) {
      synchronized (UploadServiceGrpc.class) {
        if ((getUploadMethod = UploadServiceGrpc.getUploadMethod) == null) {
          UploadServiceGrpc.getUploadMethod = getUploadMethod =
              io.grpc.MethodDescriptor.<media.upload.grpc.UploadRequest, media.upload.grpc.UploadResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Upload"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  media.upload.grpc.UploadRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  media.upload.grpc.UploadResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UploadServiceMethodDescriptorSupplier("Upload"))
              .build();
        }
      }
    }
    return getUploadMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UploadServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UploadServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UploadServiceStub>() {
        @java.lang.Override
        public UploadServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UploadServiceStub(channel, callOptions);
        }
      };
    return UploadServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static UploadServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UploadServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UploadServiceBlockingV2Stub>() {
        @java.lang.Override
        public UploadServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UploadServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return UploadServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UploadServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UploadServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UploadServiceBlockingStub>() {
        @java.lang.Override
        public UploadServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UploadServiceBlockingStub(channel, callOptions);
        }
      };
    return UploadServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UploadServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UploadServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UploadServiceFutureStub>() {
        @java.lang.Override
        public UploadServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UploadServiceFutureStub(channel, callOptions);
        }
      };
    return UploadServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default io.grpc.stub.StreamObserver<media.upload.grpc.UploadRequest> upload(
        io.grpc.stub.StreamObserver<media.upload.grpc.UploadResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getUploadMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service UploadService.
   */
  public static abstract class UploadServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return UploadServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service UploadService.
   */
  public static final class UploadServiceStub
      extends io.grpc.stub.AbstractAsyncStub<UploadServiceStub> {
    private UploadServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UploadServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UploadServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<media.upload.grpc.UploadRequest> upload(
        io.grpc.stub.StreamObserver<media.upload.grpc.UploadResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getUploadMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service UploadService.
   */
  public static final class UploadServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<UploadServiceBlockingV2Stub> {
    private UploadServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UploadServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UploadServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<media.upload.grpc.UploadRequest, media.upload.grpc.UploadResponse>
        upload() {
      return io.grpc.stub.ClientCalls.blockingBidiStreamingCall(
          getChannel(), getUploadMethod(), getCallOptions());
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service UploadService.
   */
  public static final class UploadServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<UploadServiceBlockingStub> {
    private UploadServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UploadServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UploadServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service UploadService.
   */
  public static final class UploadServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<UploadServiceFutureStub> {
    private UploadServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UploadServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UploadServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_UPLOAD = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPLOAD:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.upload(
              (io.grpc.stub.StreamObserver<media.upload.grpc.UploadResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getUploadMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              media.upload.grpc.UploadRequest,
              media.upload.grpc.UploadResponse>(
                service, METHODID_UPLOAD)))
        .build();
  }

  private static abstract class UploadServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UploadServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return media.upload.grpc.Upload.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UploadService");
    }
  }

  private static final class UploadServiceFileDescriptorSupplier
      extends UploadServiceBaseDescriptorSupplier {
    UploadServiceFileDescriptorSupplier() {}
  }

  private static final class UploadServiceMethodDescriptorSupplier
      extends UploadServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    UploadServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UploadServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UploadServiceFileDescriptorSupplier())
              .addMethod(getUploadMethod())
              .build();
        }
      }
    }
    return result;
  }
}
