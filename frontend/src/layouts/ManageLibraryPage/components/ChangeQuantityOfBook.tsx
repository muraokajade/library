import { useEffect, useState } from "react";
import BookModel from "../../../models/BookModel";
import { useAuth } from "../../../hooks/useAuth";

type ChangeQuantityOfBookProp = {
  book: BookModel;
  deleteBook: () => void;
};

export const ChangeQuantityOfBook: React.FC<ChangeQuantityOfBookProp> = ({
  book,
  deleteBook,
}) => {
  const { isAuthenticated, idToken, loading } = useAuth();
  const [quantity, setQuantity] = useState<number>(0);
  const [remaining, setRemaining] = useState<number>(0);

  useEffect(() => {
    const fetchBookInState = () => {
      book.copies ? setQuantity(book.copies) : setQuantity(0);
      book.copiesAvailable
        ? setRemaining(book.copiesAvailable)
        : setRemaining(0);
    };
    fetchBookInState();
  }, []);

  const handleDelete = async () => {
    if (loading) return;
    const url = `/api/admin/secure/delete/book?bookId=${book.id}`;

    const requestOptions = {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${idToken}`,
        "Content-Type": "application/json",
      },
    };
    const res = await fetch(url, requestOptions);
    if (!res.ok) throw new Error("本削除失敗");

    deleteBook();
  };

  return (
    <div className="card mt-3 shadow p-3 mb-3 bg-body rounded">
      <div className="row g-0">
        <div className="col-md-2">
          <div className="d-none d-lg-block">
            {book.img ? (
              <img src={book.img} width="123" height="196" alt="Book" />
            ) : (
              <img
                src={require("./../../../Images/BooksImages/book-luv2code-1000.png")}
                width="123"
                height="196"
                alt="Book"
              />
            )}
          </div>
          <div className="d-lg-none d-flex justify-content-center align-items-center">
            {book.img ? (
              <img src={book.img} width="123" height="196" alt="Book" />
            ) : (
              <img
                src={require("./../../../Images/BooksImages/book-luv2code-1000.png")}
                width="123"
                height="196"
                alt="Book"
              />
            )}
          </div>
        </div>
        <div className="col-md-6">
          <div className="card-body">
            <h5 className="card-title">{book.author}</h5>
            <h4>{book.title}</h4>
            <p className="card-text"> {book.description} </p>
          </div>
        </div>
        <div className="mt-3 col-md-4">
          <div className="d-flex justify-content-center align-items-center">
            <p>
              Total Quantity: <b>{quantity}</b>
            </p>
          </div>
          <div className="d-flex justify-content-center align-items-center">
            <p>
              Books Remaining: <b>{remaining}</b>
            </p>
          </div>
        </div>
        <div className="mt-3 col-md-1">
          <div className="d-flex justify-content-start">
            <button className="m-1 btn btn-md btn-danger" onClick={handleDelete}>Delete</button>
          </div>
        </div>
        <button className="m1 btn btn-md main-color text-white">
          Add Quantity
        </button>
        <button className="m1 btn btn-md btn-warning">Decrease Quantity</button>
      </div>
    </div>
  );
};
