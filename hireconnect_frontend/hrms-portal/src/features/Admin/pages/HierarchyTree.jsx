import React, { useState } from "react";

const initialData = {
  id: 1,
  name: "Arjun Kumar",
  designation: "CEO",
  photo: "https://i.pravatar.cc/150?img=1",
  children: [
    {
      id: 2,
      name: "Prem",
      designation: "Engineering Manager",
      photo: "https://i.pravatar.cc/150?img=2",
      children: [
        {
          id: 3,
          name: "You",
          designation: "Software Engineer",
          photo: "https://i.pravatar.cc/150?img=3",
          children: []
        }
      ]
    }
  ]
};

let idCounter = 100;

const TreeNode = ({ node, onAdd, onEdit, onDelete }) => {
  return (
    <div className="flex flex-col items-center relative">
      {/* NODE */}
      <div className="bg-white rounded-xl shadow-md px-4 py-3 w-44 text-center">
        <img
          src={node.photo}
          alt={node.name}
          className="w-20 h-20 rounded-full mx-auto object-cover border-2 border-blue-600"
        />

        <p className="mt-2 font-semibold text-sm">{node.name}</p>
        <p className="text-xs text-gray-500">{node.designation}</p>

        <div className="flex justify-center gap-2 mt-2">
          <button
            onClick={() => onAdd(node.id)}
            className="text-green-600 hover:text-green-800 text-sm"
            title="Add"
          >
            ➕
          </button>
          <button
            onClick={() => onEdit(node.id)}
            className="text-blue-600 hover:text-blue-800 text-sm"
            title="Edit"
          >
            ✏️
          </button>
          <button
            onClick={() => onDelete(node.id)}
            className="text-red-600 hover:text-red-800 text-sm"
            title="Delete"
          >
            🗑️
          </button>
        </div>
      </div>

      {/* CHILDREN */}
      {node.children.length > 0 && (
        <div className="flex gap-10 mt-8">
          {node.children.map((child) => (
            <TreeNode
              key={child.id}
              node={child}
              onAdd={onAdd}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))}
        </div>
      )}
    </div>
  );
};

const HierarchyTree = () => {
  const [tree, setTree] = useState(initialData);

  const addNode = (id) => {
    const name = prompt("Enter name");
    const designation = prompt("Enter designation");

    if (!name || !designation) return;

    const newNode = {
      id: idCounter++,
      name,
      designation,
      photo: `https://i.pravatar.cc/150?img=${idCounter}`,
      children: []
    };

    const addRecursively = (node) => {
      if (node.id === id) {
        node.children.push(newNode);
      } else {
        node.children.forEach(addRecursively);
      }
    };

    const updated = structuredClone(tree);
    addRecursively(updated);
    setTree(updated);
  };

  const editNode = (id) => {
    const editRecursively = (node) => {
      if (node.id === id) {
        node.name = prompt("Edit name", node.name) || node.name;
        node.designation =
          prompt("Edit designation", node.designation) || node.designation;
      } else {
        node.children.forEach(editRecursively);
      }
    };

    const updated = structuredClone(tree);
    editRecursively(updated);
    setTree(updated);
  };

  const deleteNode = (id) => {
    if (tree.id === id) {
      alert("Cannot delete CEO");
      return;
    }

    const deleteRecursively = (node) => {
      node.children = node.children.filter((child) => {
        if (child.id === id) return false;
        deleteRecursively(child);
        return true;
      });
    };

    const updated = structuredClone(tree);
    deleteRecursively(updated);
    setTree(updated);
  };

  return (
    <div className="min-h-screen bg-gray-100 p-10 overflow-auto">
      <h1 className="text-xl font-bold mb-8 text-center">
        Company Hierarchy
      </h1>

      <div className="flex justify-center">
        <TreeNode
          node={tree}
          onAdd={addNode}
          onEdit={editNode}
          onDelete={deleteNode}
        />
      </div>
    </div>
  );
};

export default HierarchyTree;
