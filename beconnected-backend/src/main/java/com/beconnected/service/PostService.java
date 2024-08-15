package com.beconnected.service;

import com.beconnected.model.Comment;
import com.beconnected.model.Like;
import com.beconnected.model.Post;
import com.beconnected.model.User;
import com.beconnected.repository.CommentRepository;
import com.beconnected.repository.LikeRepository;
import com.beconnected.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    public Post createPost(String textContent, byte[] mediaContent, String mediaType, User author) {
        Post post = new Post(textContent, mediaContent, mediaType, author);
        return postRepository.save(post);
    }

    public Post findById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }

    public List<Post> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorUserIdOrderByCreatedAtDesc(authorId);
    }

    public List<Post> getPostsLikedByUser(Long userId) {
        List<Like> likes = likeRepository.findByUserUserId(userId);
        return likes.stream().map(Like::getPost).distinct().collect(Collectors.toList());
    }

    public List<Post> getPostsCommentedByUser(Long userId) {
        List<Comment> comments = commentRepository.findByUserUserId(userId);
        return comments.stream().map(Comment::getPost).distinct().collect(Collectors.toList());
    }

    public List<Post> getFeedForUser(List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return new ArrayList<>();
        }
        return postRepository.findByAuthorInOrderByCreatedAtDesc(authorIds);
    }

    public void addComment(Long postId, String commentText, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        Comment comment = new Comment(post, user, commentText);
        commentRepository.save(comment);
    }

    public void likePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Optional<Like> existingLike = likeRepository.findByPostPostIdAndUserUserId(postId, user.getUserId());

        if (existingLike.isPresent()) {
            throw new RuntimeException("User has already liked this post");
        }

        Like like = new Like(post, user);
        likeRepository.save(like);
    }


    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostPostId(postId);
    }

    public List<Like> getLikesByPostId(Long postId) {
        return likeRepository.findByPostPostId(postId);
    }

    public void removeLike(Long postId, User user) {
        Like like = likeRepository.findByPostPostIdAndUserUserId(postId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Like not found"));

        likeRepository.delete(like);
    }

    public void removeComment(Long postId, Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().equals(user) || !comment.getPost().getPostId().equals(postId)) {
            throw new RuntimeException("User not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }


}
